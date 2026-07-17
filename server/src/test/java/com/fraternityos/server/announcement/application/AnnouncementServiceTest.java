package com.fraternityos.server.announcement.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fraternityos.server.announcement.application.dto.AnnouncementResponse;
import com.fraternityos.server.announcement.application.dto.CreateAnnouncementRequest;
import com.fraternityos.server.announcement.application.dto.UpdateAnnouncementRequest;
import com.fraternityos.server.announcement.domain.Announcement;
import com.fraternityos.server.announcement.infrastructure.AnnouncementRepository;
import com.fraternityos.server.house.infrastructure.MemberName;
import com.fraternityos.server.house.infrastructure.MembershipRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link AnnouncementService}: house-scoped CRUD, author-name
 * resolution, and the not-found guard that also enforces tenancy isolation.
 */
class AnnouncementServiceTest {

    private static final long HOUSE = 1L;
    private static final long AUTHOR = 10L;

    private AnnouncementRepository announcementRepository;
    private MembershipRepository membershipRepository;
    private AnnouncementService service;

    @BeforeEach
    void setUp() {
        announcementRepository = mock(AnnouncementRepository.class);
        membershipRepository = mock(MembershipRepository.class);
        service = new AnnouncementService(announcementRepository, membershipRepository);
    }

    @Test
    void create_persistsAndReturnsResponseWithAuthorName() {
        when(announcementRepository.save(any(Announcement.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(membershipRepository.findMemberName(AUTHOR, HOUSE)).thenReturn(Optional.of("Alice"));

        AnnouncementResponse response = service.create(HOUSE, AUTHOR,
                new CreateAnnouncementRequest("Water outage", "No water tomorrow", true));

        assertThat(response.title()).isEqualTo("Water outage");
        assertThat(response.content()).isEqualTo("No water tomorrow");
        assertThat(response.pinned()).isTrue();
        assertThat(response.authorMembershipId()).isEqualTo(AUTHOR);
        assertThat(response.authorName()).isEqualTo("Alice");
    }

    @Test
    void list_resolvesEachAuthorNameFromMemberships() {
        when(membershipRepository.findMemberNamesByHouseId(HOUSE))
                .thenReturn(List.of(memberName(10L, "Alice"), memberName(20L, "Bob")));
        when(announcementRepository.findAllByHouseIdOrderByPinnedDescCreatedAtDesc(HOUSE))
                .thenReturn(List.of(
                        new Announcement(HOUSE, 10L, "Pinned", "body", true),
                        new Announcement(HOUSE, 20L, "Normal", "body", false)));

        List<AnnouncementResponse> result = service.list(HOUSE);

        assertThat(result).extracting(AnnouncementResponse::authorName)
                .containsExactly("Alice", "Bob");
    }

    @Test
    void update_mutatesFieldsAndReturnsUpdatedResponse() {
        Announcement existing = new Announcement(HOUSE, AUTHOR, "Old", "old body", false);
        when(announcementRepository.findByIdAndHouseId(5L, HOUSE)).thenReturn(Optional.of(existing));
        when(announcementRepository.save(any(Announcement.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(membershipRepository.findMemberName(AUTHOR, HOUSE)).thenReturn(Optional.of("Alice"));

        AnnouncementResponse response = service.update(HOUSE, 5L,
                new UpdateAnnouncementRequest("New", "new body", true));

        assertThat(existing.getTitle()).isEqualTo("New");
        assertThat(existing.getContent()).isEqualTo("new body");
        assertThat(existing.isPinned()).isTrue();
        assertThat(response.title()).isEqualTo("New");
        assertThat(response.authorName()).isEqualTo("Alice");
    }

    @Test
    void update_missingOrOtherHouse_throwsNotFound() {
        when(announcementRepository.findByIdAndHouseId(99L, HOUSE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(HOUSE, 99L,
                new UpdateAnnouncementRequest("x", "y", false)))
                .isInstanceOf(AnnouncementNotFoundException.class);
    }

    @Test
    void delete_existing_removesIt() {
        Announcement existing = new Announcement(HOUSE, AUTHOR, "T", "B", false);
        when(announcementRepository.findByIdAndHouseId(5L, HOUSE)).thenReturn(Optional.of(existing));

        service.delete(HOUSE, 5L);

        verify(announcementRepository).delete(existing);
    }

    @Test
    void delete_missingOrOtherHouse_throwsNotFoundAndDeletesNothing() {
        when(announcementRepository.findByIdAndHouseId(99L, HOUSE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(HOUSE, 99L))
                .isInstanceOf(AnnouncementNotFoundException.class);
        verify(announcementRepository, never()).delete(any());
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
