package com.fraternityos.server.responsibility.infrastructure;

import com.fraternityos.server.responsibility.domain.ResponsibilityAssignment;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResponsibilityAssignmentRepository
        extends JpaRepository<ResponsibilityAssignment, Long> {

    List<ResponsibilityAssignment> findByResponsibilityIdIn(Collection<Long> responsibilityIds);

    Optional<ResponsibilityAssignment> findFirstByResponsibilityId(Long responsibilityId);

    void deleteByResponsibilityId(Long responsibilityId);
}
