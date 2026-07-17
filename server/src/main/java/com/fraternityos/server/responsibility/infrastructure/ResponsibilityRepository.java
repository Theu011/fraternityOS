package com.fraternityos.server.responsibility.infrastructure;

import com.fraternityos.server.responsibility.domain.Responsibility;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResponsibilityRepository extends JpaRepository<Responsibility, Long> {

    List<Responsibility> findAllByHouseIdOrderByCreatedAtDesc(Long houseId);

    /** Tenancy-scoped lookup: returns the responsibility only if it belongs to the house. */
    Optional<Responsibility> findByIdAndHouseId(Long id, Long houseId);
}
