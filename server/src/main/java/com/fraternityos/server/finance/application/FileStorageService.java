package com.fraternityos.server.finance.application;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Stores statement attachments behind a relative key. The local-disk
 * implementation can be swapped for object storage (S3) without touching
 * callers — the domain only ever sees the returned key.
 */
public interface FileStorageService {

    /** Stores the file and returns its relative storage key. */
    String store(MultipartFile file);

    /** Loads a previously stored file by its key. */
    Resource load(String key);

    /** Removes a stored file; a missing file is not an error. */
    void delete(String key);
}
