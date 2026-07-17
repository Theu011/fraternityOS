package com.fraternityos.server.finance.application;

import com.fraternityos.server.finance.application.dto.AttachmentDownload;
import com.fraternityos.server.finance.application.dto.PaymentResponse;
import com.fraternityos.server.finance.application.dto.StatementResponse;
import com.fraternityos.server.finance.domain.MonthlyStatement;
import com.fraternityos.server.finance.domain.Payment;
import com.fraternityos.server.finance.domain.PaymentStatus;
import com.fraternityos.server.finance.infrastructure.MonthlyStatementRepository;
import com.fraternityos.server.finance.infrastructure.PaymentRepository;
import com.fraternityos.server.house.domain.MemberStatus;
import com.fraternityos.server.house.domain.Membership;
import com.fraternityos.server.house.infrastructure.MemberName;
import com.fraternityos.server.house.infrastructure.MembershipRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Monthly statements and per-member payments, scoped to the caller's house.
 * Uploading a statement fans out a PENDING payment to every ACTIVE membership;
 * each member then marks their own payment paid.
 */
@Service
public class StatementService {

    private final MonthlyStatementRepository statementRepository;
    private final PaymentRepository paymentRepository;
    private final MembershipRepository membershipRepository;
    private final FileStorageService fileStorage;

    public StatementService(MonthlyStatementRepository statementRepository,
                            PaymentRepository paymentRepository,
                            MembershipRepository membershipRepository,
                            FileStorageService fileStorage) {
        this.statementRepository = statementRepository;
        this.paymentRepository = paymentRepository;
        this.membershipRepository = membershipRepository;
        this.fileStorage = fileStorage;
    }

    @Transactional
    public StatementResponse upload(Long houseId, Long uploadedByMembershipId, int month, int year,
                                    String notes, BigDecimal amountPerMember, MultipartFile file) {
        if (month < 1 || month > 12) {
            throw new InvalidFileException("Month must be between 1 and 12.");
        }
        if (statementRepository.existsByHouseIdAndYearAndMonth(houseId, year, month)) {
            throw new DuplicateStatementException(month, year);
        }
        BigDecimal amount = amountPerMember == null ? BigDecimal.ZERO : amountPerMember;

        String key = fileStorage.store(file);
        MonthlyStatement statement = new MonthlyStatement(houseId, uploadedByMembershipId, month, year);
        statement.setNotes(notes);
        statement.setAttachmentUrl(key);
        statement = statementRepository.save(statement);

        List<Membership> activeMembers = membershipRepository.findAllByHouseId(houseId).stream()
                .filter(m -> m.getStatus() == MemberStatus.ACTIVE)
                .toList();
        Long statementId = statement.getId();
        List<Payment> payments = activeMembers.stream()
                .map(m -> new Payment(statementId, m.getId(), amount))
                .toList();
        paymentRepository.saveAll(payments);

        return toResponse(statement, payments, uploadedByMembershipId, names(houseId));
    }

    @Transactional(readOnly = true)
    public List<StatementResponse> list(Long houseId, Long callerMembershipId) {
        List<MonthlyStatement> statements = statementRepository.findAllByHouseIdOrderByYearDescMonthDesc(houseId);
        if (statements.isEmpty()) {
            return List.of();
        }
        Map<Long, String> names = names(houseId);
        Map<Long, List<Payment>> paymentsByStatement = paymentRepository
                .findByMonthlyStatementIdIn(statements.stream().map(MonthlyStatement::getId).toList()).stream()
                .collect(Collectors.groupingBy(Payment::getMonthlyStatementId));

        return statements.stream()
                .map(s -> toResponse(s, paymentsByStatement.getOrDefault(s.getId(), List.of()),
                        callerMembershipId, names))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> payments(Long houseId, Long statementId) {
        MonthlyStatement statement = requireStatement(houseId, statementId);
        Map<Long, String> names = names(houseId);
        return paymentRepository.findByMonthlyStatementId(statement.getId()).stream()
                .map(p -> PaymentResponse.from(p, names.get(p.getMembershipId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public AttachmentDownload loadAttachment(Long houseId, Long statementId) {
        MonthlyStatement statement = requireStatement(houseId, statementId);
        String key = statement.getAttachmentUrl();
        if (key == null) {
            throw new StatementNotFoundException(statementId);
        }
        Resource resource = fileStorage.load(key);
        return new AttachmentDownload(resource, contentTypeFor(key), filenameFor(statement, key));
    }

    /** The caller marks their own payment paid. */
    @Transactional
    public void payOwn(Long houseId, Long statementId, Long membershipId) {
        requireStatement(houseId, statementId);
        Payment payment = paymentRepository
                .findByMonthlyStatementIdAndMembershipId(statementId, membershipId)
                .orElseThrow(() -> new PaymentNotFoundException(statementId));
        payment.markPaid();
    }

    @Transactional
    public void delete(Long houseId, Long statementId) {
        MonthlyStatement statement = requireStatement(houseId, statementId);
        paymentRepository.deleteByMonthlyStatementId(statement.getId());
        if (statement.getAttachmentUrl() != null) {
            fileStorage.delete(statement.getAttachmentUrl());
        }
        statementRepository.delete(statement);
    }

    private MonthlyStatement requireStatement(Long houseId, Long id) {
        return statementRepository.findByIdAndHouseId(id, houseId)
                .orElseThrow(() -> new StatementNotFoundException(id));
    }

    private Map<Long, String> names(Long houseId) {
        return membershipRepository.findMemberNamesByHouseId(houseId).stream()
                .collect(Collectors.toMap(MemberName::getMembershipId, MemberName::getName));
    }

    private StatementResponse toResponse(MonthlyStatement s, List<Payment> payments,
                                         Long callerMembershipId, Map<Long, String> names) {
        Payment mine = payments.stream()
                .filter(p -> p.getMembershipId().equals(callerMembershipId))
                .findFirst().orElse(null);
        long paid = payments.stream().filter(p -> p.getStatus() == PaymentStatus.PAID).count();
        return new StatementResponse(
                s.getId(), s.getMonth(), s.getYear(), s.getNotes(),
                names.get(s.getUploadedByMembershipId()), s.getCreatedAt(),
                s.getAttachmentUrl() != null,
                mine == null ? null : mine.getStatus(),
                mine == null ? null : mine.getAmount(),
                paid, payments.size());
    }

    private String contentTypeFor(String key) {
        if (key.endsWith(".pdf")) return "application/pdf";
        if (key.endsWith(".png")) return "image/png";
        if (key.endsWith(".jpg") || key.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }

    private String filenameFor(MonthlyStatement s, String key) {
        String ext = key.substring(key.lastIndexOf('.'));
        return "statement-" + s.getYear() + "-" + String.format("%02d", s.getMonth()) + ext;
    }
}
