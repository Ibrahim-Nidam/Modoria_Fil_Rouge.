package com.modoria.infrastructure.exceptions.storage;

import com.modoria.infrastructure.exceptions.base.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested file is not found in storage.
 */
public class FileNotFoundException extends BaseException {

    public FileNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "STR_002");
    }

    public FileNotFoundException(String filename, Throwable cause) {
        super("File not found: " + filename, cause, HttpStatus.NOT_FOUND, "STR_002");
    }

    public static FileNotFoundException byName(String filename) {
        return new FileNotFoundException("File not found: " + filename);
    }
}


