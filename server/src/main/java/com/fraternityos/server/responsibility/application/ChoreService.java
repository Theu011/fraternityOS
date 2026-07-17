package com.fraternityos.server.responsibility.application;

import com.fraternityos.server.house.application.MemberNotFoundException;
import com.fraternityos.server.house.domain.Membership;
import com.fraternityos.server.house.infrastructure.MemberName;
import com.fraternityos.server.house.infrastructure.MembershipRepository;
import com.fraternityos.server.responsibility.application.dto.ChoreResponse;
import com.fraternityos.server.responsibility.application.dto.CreateChoreRequest;
import com.fraternityos.server.responsibility.domain.AssignmentStatus;
import com.fraternityos.server.responsibility.domain.Responsibility;
import com.fraternityos.server.responsibility.domain.ResponsibilityAssignment;
import com.fraternityos.server.responsibility.domain.ResponsibilityType;
import com.fraternityos.server.responsibility.infrastructure.ResponsibilityAssignmentRepository;
import com.fraternityos.server.responsibility.infrastructure.ResponsibilityRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Chores for Task 7: each chore is a FIXED {@link Responsibility} paired with a
 * single {@link ResponsibilityAssignment} (assignee membership, due date,
 * status). The chore id is the responsibility id. Reads are open to any member;
 * creating and deleting are President-only; completing is allowed for the
 * assignee or a President.
 */
@Service
public class ChoreService {

    private final ResponsibilityRepository responsibilityRepository;
    private final ResponsibilityAssignmentRepository assignmentRepository;
    private final MembershipRepository membershipRepository;

    public ChoreService(ResponsibilityRepository responsibilityRepository,
                        ResponsibilityAssignmentRepository assignmentRepository,
                        MembershipRepository membershipRepository) {
        this.responsibilityRepository = responsibilityRepository;
        this.assignmentRepository = assignmentRepository;
        this.membershipRepository = membershipRepository;
    }

    @Transactional(readOnly = true)
    public List<ChoreResponse> list(Long houseId) {
        List<Responsibility> chores = responsibilityRepository.findAllByHouseIdOrderByCreatedAtDesc(houseId);
        if (chores.isEmpty()) {
            return List.of();
        }
        Map<Long, String> names = names(houseId);
        Map<Long, ResponsibilityAssignment> byResponsibility = assignmentRepository
                .findByResponsibilityIdIn(chores.stream().map(Responsibility::getId).toList()).stream()
                .collect(Collectors.toMap(ResponsibilityAssignment::getResponsibilityId, a -> a, (a, b) -> a));

        return chores.stream()
                .map(c -> toResponse(c, byResponsibility.get(c.getId()), names))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Transactional
    public ChoreResponse create(Long houseId, CreateChoreRequest request) {
        Membership assignee = membershipRepository.findByIdAndHouseId(request.assigneeId(), houseId)
                .orElseThrow(() -> new MemberNotFoundException(request.assigneeId()));

        Responsibility chore = new Responsibility(houseId, request.title(), ResponsibilityType.FIXED);
        chore.setDescription(request.description());
        chore = responsibilityRepository.save(chore);

        ResponsibilityAssignment assignment = new ResponsibilityAssignment(
                chore.getId(), assignee.getId(), LocalDate.now(), request.dueDate());
        assignmentRepository.save(assignment);

        String assigneeName = membershipRepository.findMemberName(assignee.getId(), houseId).orElse(null);
        return toResponse(chore, assignment, Map.of(assignee.getId(), assigneeName == null ? "" : assigneeName));
    }

    /** Assignee or President may complete. */
    @Transactional
    public ChoreResponse complete(Long houseId, Long choreId, Long principalMembershipId, boolean president) {
        Responsibility chore = requireChore(houseId, choreId);
        ResponsibilityAssignment assignment = assignmentRepository
                .findFirstByResponsibilityId(chore.getId())
                .orElseThrow(() -> new ChoreNotFoundException(choreId));

        if (!president && !assignment.getMembershipId().equals(principalMembershipId)) {
            throw new AccessDeniedException("Only the assignee or a President can complete this chore.");
        }
        assignment.markCompleted();

        return toResponse(chore, assignment, names(houseId));
    }

    @Transactional
    public void delete(Long houseId, Long choreId) {
        Responsibility chore = requireChore(houseId, choreId);
        assignmentRepository.deleteByResponsibilityId(chore.getId());
        responsibilityRepository.delete(chore);
    }

    private Responsibility requireChore(Long houseId, Long choreId) {
        return responsibilityRepository.findByIdAndHouseId(choreId, houseId)
                .orElseThrow(() -> new ChoreNotFoundException(choreId));
    }

    private Map<Long, String> names(Long houseId) {
        return membershipRepository.findMemberNamesByHouseId(houseId).stream()
                .collect(Collectors.toMap(MemberName::getMembershipId, MemberName::getName));
    }

    private ChoreResponse toResponse(Responsibility chore, ResponsibilityAssignment assignment,
                                     Map<Long, String> names) {
        if (assignment == null) {
            return null;
        }
        return new ChoreResponse(
                chore.getId(),
                chore.getTitle(),
                chore.getDescription(),
                assignment.getMembershipId(),
                names.get(assignment.getMembershipId()),
                assignment.getDueDate(),
                effectiveStatus(assignment),
                assignment.getCompletedAt());
    }

    /** PENDING assignments past their due date are reported as OVERDUE. */
    private AssignmentStatus effectiveStatus(ResponsibilityAssignment assignment) {
        if (assignment.getStatus() == AssignmentStatus.PENDING
                && assignment.getDueDate().isBefore(LocalDate.now())) {
            return AssignmentStatus.OVERDUE;
        }
        return assignment.getStatus();
    }
}
