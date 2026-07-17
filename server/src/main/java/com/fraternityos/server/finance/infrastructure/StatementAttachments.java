package com.fraternityos.server.finance.infrastructure;

import com.fraternityos.server.finance.application.InvalidFileException;
import java.util.Set;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

/**
 * Shared validation and key generation for statement attachments, so every
 * {@link com.fraternityos.server.finance.application.FileStorageService}
 * implementation enforces the same rules and key shape.
 */
final class StatementAttachments {

    static final Set<String> ALLOWED_TYPES =
            Set.of("application/pdf", "image/png", "image/jpeg");

    private StatementAttachments() {
    }

    /** Validates the upload and returns a fresh relative key ({@code statements/<uuid>.<ext>}). */
    static String validatedKey(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is required.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new InvalidFileException("Only PDF, PNG, or JPEG files are allowed.");
        }
        return "statements/" + UUID.randomUUID() + extensionFor(contentType);
    }

    /** Rejects keys that try to escape their prefix; keys are server-generated, so this is defensive. */
    static void rejectTraversal(String key) {
        if (key == null || key.contains("..") || key.startsWith("/")) {
            throw new InvalidFileException("Invalid storage key.");
        }
    }

    static String extensionFor(String contentType) {
        return switch (contentType) {
            case "application/pdf" -> ".pdf";
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            default -> "";
        };
    }
}
