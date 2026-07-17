package com.fraternityos.server.finance.infrastructure;

import com.fraternityos.server.finance.domain.Payment;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByMonthlyStatementId(Long monthlyStatementId);

    List<Payment> findByMonthlyStatementIdIn(Collection<Long> monthlyStatementIds);

    Optional<Payment> findByMonthlyStatementIdAndMembershipId(Long monthlyStatementId, Long membershipId);

    void deleteByMonthlyStatementId(Long monthlyStatementId);
}
