package com.fraternityos.server.announcement.infrastructure;

import com.fraternityos.server.announcement.domain.Announcement;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    /** Feed order: pinned first, then newest. */
    List<Announcement> findAllByHouseIdOrderByPinnedDescCreatedAtDesc(Long houseId);

    /** Tenancy-scoped lookup: returns the announcement only if it belongs to the house. */
    Optional<Announcement> findByIdAndHouseId(Long id, Long houseId);
}
