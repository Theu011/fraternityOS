package com.fraternityos.server.responsibility.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fraternityos.server.house.application.MemberNotFoundException;
import com.fraternityos.server.house.domain.Membership;
import com.fraternityos.server.house.infrastructure.MemberName;
import com.fraternityos.server.house.infrastructure.MembershipRepository;
import com.fraternityos.server.responsibility.application.dto.ChoreResponse;
import com.fraternityos.server.responsibility.application.dto.CreateChoreRequest;
import com.fraternityos.server.responsibility.domain.AssignmentStatus;
import com.fraternityos.server.responsibility.domain.Responsibility;
import com.fraternityos.server.responsibility.domain.ResponsibilityAssignment;
import com.fraternityos.server.responsibility.infrastructure.ResponsibilityAssignmentRepository;
import com.fraternityos.server.responsibility.infrastructure.ResponsibilityRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

/**
 * Unit tests for {@link ChoreService}: creation guards, the assignee/President
 * completion rule, the derived OVERDUE status, and tenancy-scoped not-found
 * handling — all with repositories mocked.
 */
class ChoreServiceTest {

    private static final long HOUSE = 1L;
    private static final long CHORE_ID = 100L;
    private static final long ASSIGNEE = 5L;

    private ResponsibilityRepository responsibilityRepository;
    private ResponsibilityAssignmentRepository assignmentRepository;
    private MembershipRepository membershipRepository;
    private ChoreService service;

    @BeforeEach
    void setUp() {
        responsibilityRepository = mock(ResponsibilityRepository.class);
        assignmentRepository = mock(ResponsibilityAssignmentRepository.class);
        membershipRepository = mock(MembershipRepository.class);
        service = new ChoreService(responsibilityRepository, assignmentRepository,
                membershipRepository);
    }

    @Test
    void create_unknownAssignee_throwsMemberNotFound() {
        when(membershipRepository.findByIdAndHouseId(ASSIGNEE, HOUSE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(HOUSE, new CreateChoreRequest(
                "Trash", "take it out", ASSIGNEE, LocalDate.now().plusDays(1))))
                .isInstanceOf(MemberNotFoundException.class);

        verify(responsibilityRepository, never()).save(any());
    }

    @Test
    void create_validAssignee_persistsChoreAndPendingAssignment() {
        Membership assignee = mock(Membership.class);
        when(assignee.getId()).thenReturn(ASSIGNEE);
        when(membershipRepository.findByIdAndHouseId(ASSIGNEE, HOUSE))
                .thenReturn(Optional.of(assignee));

        Responsibility saved = mock(Responsibility.class);
        when(saved.getId()).thenReturn(CHORE_ID);
        when(saved.getTitle()).thenReturn("Trash");
        when(saved.getDescription()).thenReturn("take it out");
        when(responsibilityRepository.save(any(Responsibility.class))).thenReturn(saved);
        when(membershipRepository.findMemberName(ASSIGNEE, HOUSE)).thenReturn(Optional.of("Alice"));

        LocalDate due = LocalDate.now().plusDays(3);
        ChoreResponse response = service.create(HOUSE,
                new CreateChoreRequest("Trash", "take it out", ASSIGNEE, due));

        assertThat(response.id()).isEqualTo(CHORE_ID);
        assertThat(response.title()).isEqualTo("Trash");
        assertThat(response.assigneeId()).isEqualTo(ASSIGNEE);
        assertThat(response.assigneeName()).isEqualTo("Alice");
        assertThat(response.dueDate()).isEqualTo(due);
        assertThat(response.status()).isEqualTo(AssignmentStatus.PENDING);
        verify(assignmentRepository).save(any(ResponsibilityAssignment.class));
    }

    @Test
    void complete_byAssignee_marksCompleted() {
        stubChore();
        ResponsibilityAssignment assignment = pendingAssignment(LocalDate.now().plusDays(1));
        when(assignmentRepository.findFirstByResponsibilityId(CHORE_ID))
                .thenReturn(Optional.of(assignment));
        when(membershipRepository.findMemberNamesByHouseId(HOUSE))
                .thenReturn(List.of(memberName(ASSIGNEE, "Alice")));

        ChoreResponse response = service.complete(HOUSE, CHORE_ID, ASSIGNEE, false);

        assertThat(response.status()).isEqualTo(AssignmentStatus.COMPLETED);
        assertThat(response.completedAt()).isNotNull();
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.COMPLETED);
    }

    @Test
    void complete_byPresident_marksAnotherMembersChoreCompleted() {
        stubChore();
        ResponsibilityAssignment assignment = pendingAssignment(LocalDate.now().plusDays(1));
        when(assignmentRepository.findFirstByResponsibilityId(CHORE_ID))
                .thenReturn(Optional.of(assignment));
        when(membershipRepository.findMemberNamesByHouseId(HOUSE))
                .thenReturn(List.of(memberName(ASSIGNEE, "Alice")));

        ChoreResponse response = service.complete(HOUSE, CHORE_ID, 999L, true);

        assertThat(response.status()).isEqualTo(AssignmentStatus.COMPLETED);
    }

    @Test
    void complete_byOtherMemberWhoIsNotPresident_isDeniedAndLeavesStatusUntouched() {
        stubChore();
        ResponsibilityAssignment assignment = pendingAssignment(LocalDate.now().plusDays(1));
        when(assignmentRepository.findFirstByResponsibilityId(CHORE_ID))
                .thenReturn(Optional.of(assignment));

        assertThatThrownBy(() -> service.complete(HOUSE, CHORE_ID, 999L, false))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.PENDING);
    }

    @Test
    void complete_unknownChore_throwsChoreNotFound() {
        when(responsibilityRepository.findByIdAndHouseId(CHORE_ID, HOUSE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.complete(HOUSE, CHORE_ID, ASSIGNEE, false))
                .isInstanceOf(ChoreNotFoundException.class);
    }

    @Test
    void list_pendingAssignmentPastDue_isReportedAsOverdue() {
        Responsibility chore = mock(Responsibility.class);
        when(chore.getId()).thenReturn(CHORE_ID);
        when(chore.getTitle()).thenReturn("Trash");
        when(responsibilityRepository.findAllByHouseIdOrderByCreatedAtDesc(HOUSE))
                .thenReturn(List.of(chore));
        when(membershipRepository.findMemberNamesByHouseId(HOUSE))
                .thenReturn(List.of(memberName(ASSIGNEE, "Alice")));
        when(assignmentRepository.findByResponsibilityIdIn(List.of(CHORE_ID)))
                .thenReturn(List.of(pendingAssignment(LocalDate.now().minusDays(1))));

        List<ChoreResponse> result = service.list(HOUSE);

        assertThat(result).singleElement()
                .satisfies(c -> assertThat(c.status()).isEqualTo(AssignmentStatus.OVERDUE));
    }

    @Test
    void delete_existingChore_removesAssignmentThenChore() {
        Responsibility chore = stubChore();

        service.delete(HOUSE, CHORE_ID);

        verify(assignmentRepository).deleteByResponsibilityId(CHORE_ID);
        verify(responsibilityRepository).delete(chore);
    }

    @Test
    void delete_unknownChore_throwsChoreNotFound() {
        when(responsibilityRepository.findByIdAndHouseId(CHORE_ID, HOUSE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(HOUSE, CHORE_ID))
                .isInstanceOf(ChoreNotFoundException.class);
    }

    private Responsibility stubChore() {
        Responsibility chore = mock(Responsibility.class);
        when(chore.getId()).thenReturn(CHORE_ID);
        when(chore.getTitle()).thenReturn("Trash");
        when(responsibilityRepository.findByIdAndHouseId(CHORE_ID, HOUSE))
                .thenReturn(Optional.of(chore));
        return chore;
    }

    private static ResponsibilityAssignment pendingAssignment(LocalDate dueDate) {
        return new ResponsibilityAssignment(CHORE_ID, ASSIGNEE, LocalDate.now().minusDays(2), dueDate);
    }

    private static MemberName memberName(Long membershipId, String name) {
        return new MemberName() {
            @Override
            public Long getMembershipId() {
                return membershipId;
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }
}
