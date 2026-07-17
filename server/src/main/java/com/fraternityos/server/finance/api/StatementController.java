package com.fraternityos.server.finance.api;

import com.fraternityos.server.auth.security.AuthenticatedMember;
import com.fraternityos.server.finance.application.StatementService;
import com.fraternityos.server.finance.application.dto.AttachmentDownload;
import com.fraternityos.server.finance.application.dto.PaymentResponse;
import com.fraternityos.server.finance.application.dto.StatementResponse;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Finance. Any member can view statements, open the attachment, and mark their
 * own payment paid. Only Treasurers upload or delete statements; Treasurers and
 * Presidents can see the full payment roster.
 */
@RestController
@RequestMapping("/statements")
public class StatementController {

    private final StatementService statementService;

    public StatementController(StatementService statementService) {
        this.statementService = statementService;
    }

    @GetMapping
    public List<StatementResponse> list(@AuthenticationPrincipal AuthenticatedMember principal) {
        return statementService.list(principal.houseId(), principal.membershipId());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TREASURER')")
    @ResponseStatus(HttpStatus.CREATED)
    public StatementResponse upload(@AuthenticationPrincipal AuthenticatedMember principal,
                                    @RequestParam int month,
                                    @RequestParam int year,
                                    @RequestParam(required = false) String notes,
                                    @RequestParam(required = false) BigDecimal amountPerMember,
                                    @RequestParam("file") MultipartFile file) {
        return statementService.upload(principal.houseId(), principal.membershipId(),
                month, year, notes, amountPerMember, file);
    }

    @GetMapping("/{id}/attachment")
    public ResponseEntity<Resource> attachment(@AuthenticationPrincipal AuthenticatedMember principal,
                                               @PathVariable Long id) {
        AttachmentDownload download = statementService.loadAttachment(principal.houseId(), id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + download.filename() + "\"")
                .body(download.resource());
    }

    @PostMapping("/{id}/pay")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void pay(@AuthenticationPrincipal AuthenticatedMember principal, @PathVariable Long id) {
        statementService.payOwn(principal.houseId(), id, principal.membershipId());
    }

    @GetMapping("/{id}/payments")
    @PreAuthorize("hasAnyRole('TREASURER', 'PRESIDENT')")
    public List<PaymentResponse> payments(@AuthenticationPrincipal AuthenticatedMember principal,
                                          @PathVariable Long id) {
        return statementService.payments(principal.houseId(), id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TREASURER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthenticatedMember principal, @PathVariable Long id) {
        statementService.delete(principal.houseId(), id);
    }
}
