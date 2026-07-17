package com.fraternityos.server.announcement.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.fraternityos.server.announcement.domain.Announcement;
import com.fraternityos.server.house.domain.House;
import com.fraternityos.server.house.domain.MemberStatus;
import com.fraternityos.server.house.domain.Membership;
import com.fraternityos.server.house.domain.User;
import com.fraternityos.server.house.infrastructure.HouseRepository;
import com.fraternityos.server.house.infrastructure.MembershipRepository;
import com.fraternityos.server.house.infrastructure.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Slice test for {@link AnnouncementRepository} against a real PostgreSQL (via
 * Testcontainers, so the derived queries run on the actual schema). Verifies the
 * feed ordering and the tenancy-scoped lookup.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class AnnouncementRepositoryTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Test
    void findAllByHouse_ordersPinnedFirst() {
        long membershipId = seedMembership("feed@x.com");
        long houseId = membershipRepository.findById(membershipId).orElseThrow().getHouseId();

        announcementRepository.save(new Announcement(houseId, membershipId, "Normal A", "body", false));
        announcementRepository.save(new Announcement(houseId, membershipId, "Pinned", "body", true));
        announcementRepository.save(new Announcement(houseId, membershipId, "Normal B", "body", false));

        List<Announcement> feed =
                announcementRepository.findAllByHouseIdOrderByPinnedDescCreatedAtDesc(houseId);

        assertThat(feed).hasSize(3);
        assertThat(feed.get(0).isPinned()).isTrue();
        assertThat(feed).filteredOn(a -> !a.isPinned()).hasSize(2);
    }

    @Test
    void findByIdAndHouseId_isScopedToTheOwningHouse() {
        long membershipId = seedMembership("scope@x.com");
        long houseId = membershipRepository.findById(membershipId).orElseThrow().getHouseId();
        Announcement saved = announcementRepository.save(
                new Announcement(houseId, membershipId, "Mine", "body", false));

        assertThat(announcementRepository.findByIdAndHouseId(saved.getId(), houseId)).isPresent();
        assertThat(announcementRepository.findByIdAndHouseId(saved.getId(), houseId + 999)).isEmpty();
    }

    private long seedMembership(String email) {
        House house = houseRepository.save(new House("Repo House"));
        User user = userRepository.save(new User("Repo User", email, "hash"));
        Membership membership = membershipRepository.save(
                new Membership(user.getId(), house.getId(), MemberStatus.ACTIVE));
        return membership.getId();
    }
}
