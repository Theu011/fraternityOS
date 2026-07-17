package com.fraternityos.server.finance.application;

/** Thrown for empty or unsupported uploads (only PDF/PNG/JPEG are allowed). */
public class InvalidFileException extends RuntimeException {

    public InvalidFileException(String message) {
        super(message);
    }
}
