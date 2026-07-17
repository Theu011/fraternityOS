package com.fraternityos.server.house.infrastructure;

import com.fraternityos.server.house.domain.Position;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, Long> {

    Optional<Position> findByName(String name);

    List<Position> findByNameIn(Collection<String> names);

    List<Position> findAllByOrderByNameAsc();
}
