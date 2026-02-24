package com.modoria.infrastructure.exceptions.storage;

import com.modoria.infrastructure.exceptions.base.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when file upload fails.
 */
public class FileUploadException extends BaseException {

    public FileUploadException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "STR_001");
    }

    public FileUploadException(String message, Throwable cause) {
        super(message, cause, HttpStatus.INTERNAL_SERVER_ERROR, "STR_001");
    }

    public static FileUploadException emptyFile() {
        return new FileUploadException("Cannot upload empty file");
    }

    public static FileUploadException invalidType(String contentType) {
        return new FileUploadException("Invalid file type: " + contentType);
    }

    public static FileUploadException tooLarge(long size, long maxSize) {
        return new FileUploadException(
                String.format("File size %d exceeds maximum allowed size %d", size, maxSize));
    }

    public static FileUploadException storageError(Throwable cause) {
        return new FileUploadException("Failed to store file", cause);
    }
}


