package com.modoria.infrastructure.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for file storage operations.
 * Supports both local and cloud storage through configuration.
 */
public interface StorageService {

    /**
     * Store a file and return its URL.
     */
    String store(MultipartFile file);

    /**
     * Store a file with a custom filename.
     */
    String store(MultipartFile file, String filename);

    /**
     * Load a file as a Resource.
     */
    Resource load(String filename);

    /**
     * Delete a file.
     */
    void delete(String filename);

    /**
     * Check if a file exists.
     */
    boolean exists(String filename);

    /**
     * Get the full URL for a file.
     */
    String getUrl(String filename);
}
