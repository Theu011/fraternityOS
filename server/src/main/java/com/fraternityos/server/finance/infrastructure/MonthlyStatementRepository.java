package com.fraternityos.server.finance.infrastructure;

import com.fraternityos.server.finance.domain.MonthlyStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonthlyStatementRepository extends JpaRepository<MonthlyStatement, Long> {

    List<MonthlyStatement> findAllByHouseIdOrderByYearDescMonthDesc(Long houseId);

    Optional<MonthlyStatement> findByIdAndHouseId(Long id, Long houseId);

    boolean existsByHouseIdAndYearAndMonth(Long houseId, int year, int month);
}
