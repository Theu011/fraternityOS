package com.fraternityos.server.finance.application.dto;

import org.springframework.core.io.Resource;

/** A statement attachment ready to stream back to the client. */
public record AttachmentDownload(Resource resource, String contentType, String filename) {
}
