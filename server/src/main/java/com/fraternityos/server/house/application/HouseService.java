package com.fraternityos.server.house.application;

import com.fraternityos.server.auth.application.JwtService;
import com.fraternityos.server.auth.application.dto.AuthResponse;
import com.fraternityos.server.house.application.dto.HouseResponse;
import com.fraternityos.server.house.application.dto.HouseSummaryResponse;
import com.fraternityos.server.house.application.dto.JoinRequestResponse;
import com.fraternityos.server.house.application.dto.MyJoinRequestResponse;
import com.fraternityos.server.house.application.dto.PendingJoinRequestResponse;
import com.fraternityos.server.house.domain.House;
import com.fraternityos.server.house.domain.JoinRequest;
import com.fraternityos.server.house.domain.JoinRequestStatus;
import com.fraternityos.server.house.domain.MemberStatus;
import com.fraternityos.server.house.domain.Membership;
import com.fraternityos.server.house.domain.MembershipPosition;
import com.fraternityos.server.house.domain.Position;
import com.fraternityos.server.house.domain.User;
import com.fraternityos.server.house.infrastructure.HouseRepository;
import com.fraternityos.server.house.infrastructure.HouseSummary;
import com.fraternityos.server.house.infrastructure.JoinRequestRepository;
import com.fraternityos.server.house.infrastructure.MembershipPositionRepository;
import com.fraternityos.server.house.infrastructure.MembershipRepository;
import com.fraternityos.server.house.infrastructure.PositionRepository;
import com.fraternityos.server.house.infrastructure.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * House read/onboarding use cases: listing and searching houses, creating a house
 * (founder becomes President), and requesting to join an existing house. All are
 * scoped by the authenticated user derived from the principal, never client input.
 */
@Service
public class HouseService {

    private static final String PRESIDENT_POSITION = "President";

    private final HouseRepository houseRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final MembershipPositionRepository membershipPositionRepository;
    private final PositionRepository positionRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final JwtService jwtService;

    public HouseService(HouseRepository houseRepository, UserRepository userRepository,
                        MembershipRepository membershipRepository,
                        MembershipPositionRepository membershipPositionRepository,
                        PositionRepository positionRepository,
                        JoinRequestRepository joinRequestRepository,
                        JwtService jwtService) {
        this.houseRepository = houseRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.membershipPositionRepository = membershipPositionRepository;
        this.positionRepository = positionRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.jwtService = jwtService;
    }

    /** Returns the caller's house. The id comes from the authenticated principal. */
    @Transactional(readOnly = true)
    public HouseResponse getCurrent(Long houseId) {
        return houseRepository.findById(houseId)
                .map(HouseResponse::from)
                .orElseThrow(() -> new HouseNotFoundException(houseId));
    }

    /** Lists houses, optionally filtered by a case-insensitive name fragment. */
    @Transactional(readOnly = true)
    public List<HouseSummaryResponse> list(String name) {
        List<HouseSummary> summaries = (name == null || name.isBlank())
                ? houseRepository.findSummaries(MemberStatus.ACTIVE)
                : houseRepository.searchSummaries(name.trim(), MemberStatus.ACTIVE);
        return summaries.stream().map(HouseSummaryResponse::from).toList();
    }

    /**
     * Creates a house and makes the caller its founding ACTIVE President, then
     * issues a fresh token carrying the new house context (the caller's previous
     * token had no house). Fails if the caller already belongs to a house.
     */
    @Transactional
    public AuthResponse create(Long userId, String name) {
        requireNoMembership(userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user " + userId + " not found"));

        House house = houseRepository.save(new House(name.trim()));
        Membership membership = membershipRepository.save(
                new Membership(userId, house.getId(), MemberStatus.ACTIVE));

        Position president = positionRepository.findByName(PRESIDENT_POSITION)
                .orElseThrow(() -> new IllegalStateException(
                        "Seed position '" + PRESIDENT_POSITION + "' is missing"));
        membershipPositionRepository.save(
                new MembershipPosition(membership.getId(), president.getId()));

        List<String> positions = List.of(PRESIDENT_POSITION);
        String token = jwtService.generateToken(
                userId, membership.getId(), house.getId(), user.getEmail(), positions);
        return AuthResponse.bearer(token, userId, membership.getId(),
                house.getId(), user.getName(), positions);
    }

    /**
     * Submits a PENDING request for the caller to join {@code houseId}. Fails if
     * the house does not exist, the caller already belongs to a house, or an
     * identical request is already outstanding.
     */
    @Transactional
    public JoinRequestResponse requestToJoin(Long userId, Long houseId) {
        if (!houseRepository.existsById(houseId)) {
            throw new HouseNotFoundException(houseId);
        }
        requireNoMembership(userId);
        if (joinRequestRepository.existsByUserIdAndHouseIdAndStatus(
                userId, houseId, JoinRequestStatus.PENDING)) {
            throw new DuplicateJoinRequestException(houseId);
        }
        JoinRequest saved = joinRequestRepository.save(new JoinRequest(houseId, userId));
        return JoinRequestResponse.from(saved);
    }

    /** The caller's own join requests (any status), most recent first. */
    @Transactional(readOnly = true)
    public List<MyJoinRequestResponse> myJoinRequests(Long userId) {
        return joinRequestRepository.findMine(userId).stream()
                .map(MyJoinRequestResponse::from)
                .toList();
    }

    /** Pending requests to the given house (the President's), oldest first. */
    @Transactional(readOnly = true)
    public List<PendingJoinRequestResponse> pendingJoinRequests(Long houseId) {
        return joinRequestRepository.findPending(houseId, JoinRequestStatus.PENDING).stream()
                .map(PendingJoinRequestResponse::from)
                .toList();
    }

    /**
     * Approves a pending request scoped to the President's house: the requester
     * gains an ACTIVE membership and any other pending requests they hold are
     * rejected (a user belongs to at most one house).
     */
    @Transactional
    public void approve(Long houseId, Long requestId) {
        JoinRequest request = requirePending(houseId, requestId);
        Long requesterId = request.getUserId();
        if (membershipRepository.findByUserId(requesterId).isPresent()) {
            throw new AlreadyInHouseException();
        }
        membershipRepository.save(new Membership(requesterId, houseId, MemberStatus.ACTIVE));
        request.approve();
        // Close out any other outstanding requests from the same user.
        joinRequestRepository.findByUserIdAndStatus(requesterId, JoinRequestStatus.PENDING).stream()
                .filter(other -> !other.getId().equals(requestId))
                .forEach(JoinRequest::reject);
    }

    /** Rejects a pending request scoped to the President's house. */
    @Transactional
    public void reject(Long houseId, Long requestId) {
        requirePending(houseId, requestId).reject();
    }

    private JoinRequest requirePending(Long houseId, Long requestId) {
        JoinRequest request = joinRequestRepository.findById(requestId)
                .filter(r -> r.getHouseId().equals(houseId))
                .orElseThrow(() -> new JoinRequestNotFoundException(requestId));
        if (!request.isPending()) {
            throw new DuplicateJoinRequestException(houseId);
        }
        return request;
    }

    private void requireNoMembership(Long userId) {
        if (membershipRepository.findByUserId(userId).isPresent()) {
            throw new AlreadyInHouseException();
        }
    }
}
