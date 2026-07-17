package com.fraternityos.server.calendar.infrastructure;

import com.fraternityos.server.calendar.domain.Event;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByHouseIdOrderByStartDateAsc(Long houseId);

    /** Tenancy-scoped lookup: returns the event only if it belongs to the house. */
    Optional<Event> findByIdAndHouseId(Long id, Long houseId);
}
