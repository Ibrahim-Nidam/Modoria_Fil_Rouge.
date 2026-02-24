package com.modoria.infrastructure.storage.impl;

import com.modoria.infrastructure.storage.StorageService;
import com.modoria.infrastructure.utils.SlugUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileSystemStorageService implements StorageService {

    @Value("${app.storage.location:uploads}")
    private String uploadDir;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        this.rootLocation = Paths.get(uploadDir);
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        String filename = cleanFilename(file.getOriginalFilename());
        return store(file, filename);
    }

    @Override
    public String store(MultipartFile file, String filename) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file.");
            }

            // Ensure unique filename to prevent overwrites
            String uniqueFilename = UUID.randomUUID().toString() + "_" + filename;
            Path destinationFile = this.rootLocation.resolve(Paths.get(uniqueFilename))
                    .normalize().toAbsolutePath();

            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                // This is a security check
                throw new RuntimeException("Cannot store file outside current directory.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return getUrl(uniqueFilename);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }

    @Override
    public Resource load(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void delete(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            FileSystemUtils.deleteRecursively(file);
        } catch (IOException e) {
            log.error("Could not delete file: " + filename, e);
        }
    }

    @Override
    public boolean exists(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            return Files.exists(file);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getUrl(String filename) {
        // Assuming we have a controller to serve files at /api/uploads/{filename}
        // For now, returning a relative URL or full URL based on current context
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/uploads/")
                .path(filename)
                .toUriString();
    }

    private String cleanFilename(String filename) {
        if (filename == null)
            return "unknown";
        return SlugUtil.toSlug(filename);
    }
}
