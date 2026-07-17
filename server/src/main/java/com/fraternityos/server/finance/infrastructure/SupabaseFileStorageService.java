package com.fraternityos.server.finance.infrastructure;

import com.fraternityos.server.finance.application.FileStorageService;
import com.fraternityos.server.finance.application.InvalidFileException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

/**
 * Stores statement attachments in Supabase Storage (private bucket) via its REST
 * API. Active only in the {@code prod} profile; local/dev runs use
 * {@link LocalDiskFileStorageService}. Objects are addressed by the same relative
 * key ({@code statements/<uuid>.<ext>}) persisted in {@code attachment_url}, and
 * downloads are proxied through the backend using the Supabase secret key, so the
 * bucket stays private. Fixes the ephemeral-disk problem on Render: attachments
 * now survive redeploys and restarts.
 */
@Service
@Profile("prod")
public class SupabaseFileStorageService implements FileStorageService {

    /** {@code https://<ref>.supabase.co/storage/v1/object/<bucket>} */
    private final String objectBase;
    private final RestClient restClient;

    public SupabaseFileStorageService(
            @Value("${app.storage.supabase.url}") String url,
            @Value("${app.storage.supabase.bucket:statements}") String bucket,
            @Value("${app.storage.supabase.secret-key}") String secretKey) {
        String base = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        this.objectBase = base + "/storage/v1/object/" + bucket;
        // The secret key goes in both headers: `apikey` identifies the project,
        // `Authorization: Bearer` authenticates the request (bypasses RLS, backend-only).
        this.restClient = RestClient.builder()
                .defaultHeader("apikey", secretKey)
                .defaultHeader("Authorization", "Bearer " + secretKey)
                .build();
    }

    @Override
    public String store(MultipartFile file) {
        String key = StatementAttachments.validatedKey(file);
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read upload", e);
        }
        try {
            restClient.post()
                    .uri(URI.create(objectBase + "/" + key))
                    .header("Content-Type", file.getContentType())
                    .header("x-upsert", "true")
                    .body(bytes)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new UncheckedIOException(
                    "Failed to upload attachment to Supabase (HTTP " + e.getStatusCode().value() + ")",
                    new IOException(e.getResponseBodyAsString()));
        }
        return key;
    }

    @Override
    public Resource load(String key) {
        StatementAttachments.rejectTraversal(key);
        try {
            byte[] bytes = restClient.get()
                    .uri(URI.create(objectBase + "/" + key))
                    .retrieve()
                    .body(byte[].class);
            if (bytes == null || bytes.length == 0) {
                throw new InvalidFileException("Attachment not found.");
            }
            return new ByteArrayResource(bytes);
        } catch (HttpClientErrorException.NotFound e) {
            throw new InvalidFileException("Attachment not found.");
        }
    }

    @Override
    public void delete(String key) {
        StatementAttachments.rejectTraversal(key);
        try {
            restClient.delete()
                    .uri(URI.create(objectBase + "/" + key))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound e) {
            // A missing object is not an error, mirroring deleteIfExists on local disk.
        }
    }
}
