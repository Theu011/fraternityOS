package com.fraternityos.server.shared.api;

import com.fraternityos.server.announcement.application.AnnouncementNotFoundException;
import com.fraternityos.server.auth.application.EmailAlreadyUsedException;
import com.fraternityos.server.calendar.application.EventNotFoundException;
import com.fraternityos.server.finance.application.DuplicateStatementException;
import com.fraternityos.server.finance.application.InvalidFileException;
import com.fraternityos.server.finance.application.PaymentNotFoundException;
import com.fraternityos.server.finance.application.StatementNotFoundException;
import com.fraternityos.server.house.application.AlreadyInHouseException;
import com.fraternityos.server.house.application.DuplicateJoinRequestException;
import com.fraternityos.server.house.application.HouseNotFoundException;
import com.fraternityos.server.house.application.JoinRequestNotFoundException;
import com.fraternityos.server.house.application.LastPresidentException;
import com.fraternityos.server.house.application.MemberNotFoundException;
import com.fraternityos.server.house.application.PositionNotFoundException;
import com.fraternityos.server.responsibility.application.ChoreNotFoundException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<Map<String, Object>> handleEmailInUse(EmailAlreadyUsedException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleMemberNotFound(MemberNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(AnnouncementNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleAnnouncementNotFound(AnnouncementNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(HouseNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleHouseNotFound(HouseNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(JoinRequestNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleJoinRequestNotFound(JoinRequestNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEventNotFound(EventNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ChoreNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleChoreNotFound(ChoreNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({StatementNotFoundException.class, PaymentNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleFinanceNotFound(RuntimeException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({AlreadyInHouseException.class, DuplicateJoinRequestException.class,
            LastPresidentException.class})
    public ResponseEntity<Map<String, Object>> handleHouseConflict(RuntimeException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(PositionNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePositionNotFound(PositionNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DuplicateStatementException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateStatement(DuplicateStatementException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidFile(InvalidFileException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "Validation failed");
        body.put("errors", ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, this::messageOf, (a, b) -> a)));
        return ResponseEntity.badRequest().body(body);
    }

    private String messageOf(FieldError error) {
        return error.getDefaultMessage() == null ? "invalid" : error.getDefaultMessage();
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(baseBody(status, message));
    }

    private Map<String, Object> baseBody(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }
}
