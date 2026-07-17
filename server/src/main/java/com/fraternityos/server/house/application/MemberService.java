package com.fraternityos.server.house.application;

import com.fraternityos.server.auth.application.EmailAlreadyUsedException;
import com.fraternityos.server.house.application.dto.CreateMemberRequest;
import com.fraternityos.server.house.application.dto.MemberResponse;
import com.fraternityos.server.house.application.dto.UpdateMemberRequest;
import com.fraternityos.server.house.domain.MemberStatus;
import com.fraternityos.server.house.domain.Membership;
import com.fraternityos.server.house.domain.MembershipPosition;
import com.fraternityos.server.house.domain.Position;
import com.fraternityos.server.house.domain.User;
import com.fraternityos.server.house.infrastructure.MembershipPositionRepository;
import com.fraternityos.server.house.infrastructure.MembershipRepository;
import com.fraternityos.server.house.infrastructure.PositionRepository;
import com.fraternityos.server.house.infrastructure.UserRepository;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Member CRUD, always scoped to a single house. A "member" is a {@link Membership}
 * joined with its {@link User}; the {@code houseId} comes from the authenticated
 * principal, never from client input, so one house can never read or mutate
 * another's members.
 */
@Service
public class MemberService {

    private static final String PRESIDENT_POSITION = "President";

    private final MembershipRepository membershipRepository;
    private final MembershipPositionRepository membershipPositionRepository;
    private final PositionRepository positionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MembershipRepository membershipRepository,
                         MembershipPositionRepository membershipPositionRepository,
                         PositionRepository positionRepository,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder) {
        this.membershipRepository = membershipRepository;
        this.membershipPositionRepository = membershipPositionRepository;
        this.positionRepository = positionRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Active members of the house. */
    @Transactional(readOnly = true)
    public List<MemberResponse> list(Long houseId) {
        return toResponses(membershipRepository.findAllByHouseIdAndStatus(houseId, MemberStatus.ACTIVE));
    }

    /** Alumni of the house (their membership and position history are retained). */
    @Transactional(readOnly = true)
    public List<MemberResponse> listAlumni(Long houseId) {
        return toResponses(membershipRepository.findAllByHouseIdAndStatus(houseId, MemberStatus.ALUMNI));
    }

    private List<MemberResponse> toResponses(List<Membership> memberships) {
        if (memberships.isEmpty()) {
            return List.of();
        }
        Map<Long, User> usersById = userRepository
                .findAllById(memberships.stream().map(Membership::getUserId).toList()).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Long, List<String>> positionsByMembership = positionsByMembership(
                memberships.stream().map(Membership::getId).toList());

        return memberships.stream()
                .map(m -> MemberResponse.from(m, usersById.get(m.getUserId()),
                        positionsByMembership.getOrDefault(m.getId(), List.of())))
                .toList();
    }

    /**
     * Changes a member's status. Guards the "at least one active President"
     * invariant: retiring the last active President is rejected.
     */
    @Transactional
    public MemberResponse changeStatus(Long houseId, Long id, MemberStatus status) {
        Membership membership = require(houseId, id);
        if (status == MemberStatus.ALUMNI && membership.getStatus() == MemberStatus.ACTIVE
                && holdsPresident(id) && isLastActivePresident(houseId, id)) {
            throw new LastPresidentException();
        }
        membership.setStatus(status);
        membership.setGraduatedAt(status == MemberStatus.ALUMNI ? Instant.now() : null);
        membershipRepository.save(membership);
        return toResponse(membership);
    }

    /** Assigns one or more catalog positions to a member (idempotent per position). */
    @Transactional
    public MemberResponse assignPositions(Long houseId, Long id, List<Long> positionIds) {
        Membership membership = require(houseId, id);
        for (Long positionId : positionIds) {
            if (!positionRepository.existsById(positionId)) {
                throw new PositionNotFoundException(positionId);
            }
            if (!membershipPositionRepository.existsByMembershipIdAndPositionId(id, positionId)) {
                membershipPositionRepository.save(new MembershipPosition(id, positionId));
            }
        }
        return toResponse(membership);
    }

    /**
     * Removes a position from a member. Guards the "at least one active President"
     * invariant: removing President from the last active President is rejected.
     */
    @Transactional
    public MemberResponse removePosition(Long houseId, Long id, Long positionId) {
        Membership membership = require(houseId, id);
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new PositionNotFoundException(positionId));
        boolean holds = membershipPositionRepository.existsByMembershipIdAndPositionId(id, positionId);
        if (holds && PRESIDENT_POSITION.equals(position.getName())
                && membership.getStatus() == MemberStatus.ACTIVE
                && isLastActivePresident(houseId, id)) {
            throw new LastPresidentException();
        }
        if (holds) {
            membershipPositionRepository.deleteByMembershipIdAndPositionId(id, positionId);
        }
        return toResponse(membership);
    }

    private boolean holdsPresident(Long membershipId) {
        return membershipPositionRepository.findPositionNames(membershipId).contains(PRESIDENT_POSITION);
    }

    /** True when no other ACTIVE member of the house holds the President position. */
    private boolean isLastActivePresident(Long houseId, Long membershipId) {
        return membershipRepository.countHoldersOfPositionExcluding(
                houseId, MemberStatus.ACTIVE, PRESIDENT_POSITION, membershipId) == 0;
    }

    @Transactional(readOnly = true)
    public MemberResponse get(Long houseId, Long id) {
        Membership membership = require(houseId, id);
        return toResponse(membership);
    }

    @Transactional
    public MemberResponse create(Long houseId, CreateMemberRequest request) {
        String email = request.email().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException(email);
        }
        User user = new User(request.name(), email, passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user = userRepository.save(user);

        Membership membership = new Membership(user.getId(), houseId, MemberStatus.ACTIVE);
        membership.setRoom(request.room());
        membership = membershipRepository.save(membership);

        syncPositions(membership.getId(), request.positions());
        return toResponse(membership);
    }

    @Transactional
    public MemberResponse update(Long houseId, Long id, UpdateMemberRequest request) {
        Membership membership = require(houseId, id);
        User user = userRepository.findById(membership.getUserId())
                .orElseThrow(() -> new MemberNotFoundException(id));

        user.setName(request.name());
        user.setPhone(request.phone());
        userRepository.save(user);

        membership.setStatus(request.status());
        membership.setRoom(request.room());
        membershipRepository.save(membership);

        syncPositions(membership.getId(), request.positions());
        return toResponse(membership);
    }

    /**
     * "Removing" a member retires them (status ALUMNI) rather than hard-deleting.
     * Other rows reference this membership (announcements, events, chore
     * assignments, payments) with no cascade, so a hard delete would violate
     * those foreign keys; retiring also keeps the member's history, which is the
     * intended model. Reuses {@link #changeStatus} so the "at least one active
     * President" invariant is enforced here too.
     */
    @Transactional
    public void delete(Long houseId, Long id) {
        changeStatus(houseId, id, MemberStatus.ALUMNI);
    }

    private Membership require(Long houseId, Long id) {
        return membershipRepository.findByIdAndHouseId(id, houseId)
                .orElseThrow(() -> new MemberNotFoundException(id));
    }

    private MemberResponse toResponse(Membership membership) {
        User user = userRepository.findById(membership.getUserId())
                .orElseThrow(() -> new MemberNotFoundException(membership.getId()));
        return MemberResponse.from(membership, user,
                membershipPositionRepository.findPositionNames(membership.getId()));
    }

    /** Replaces a membership's positions with the named catalog entries (unknown names ignored). */
    private void syncPositions(Long membershipId, List<String> positionNames) {
        membershipPositionRepository.deleteByMembershipId(membershipId);
        if (positionNames == null || positionNames.isEmpty()) {
            return;
        }
        List<MembershipPosition> assignments = positionRepository.findByNameIn(positionNames).stream()
                .map(Position::getId)
                .map(positionId -> new MembershipPosition(membershipId, positionId))
                .toList();
        membershipPositionRepository.saveAll(assignments);
    }

    private Map<Long, List<String>> positionsByMembership(Collection<Long> membershipIds) {
        return membershipPositionRepository.findPositionNamesByMembershipIds(membershipIds).stream()
                .collect(Collectors.groupingBy(
                        MembershipPositionRepository.MembershipPositionName::getMembershipId,
                        Collectors.mapping(
                                MembershipPositionRepository.MembershipPositionName::getName,
                                Collectors.toList())));
    }
}
