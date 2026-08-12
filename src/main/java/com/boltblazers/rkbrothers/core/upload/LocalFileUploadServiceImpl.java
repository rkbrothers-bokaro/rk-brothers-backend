package com.boltblazers.rkbrothers.core.upload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Stores files on the local filesystem under app.upload.base-dir. This is a
 * development stub — GcsFileUploadServiceImpl takes over in prod.
 */
@Slf4j
@Service
@Profile("!prod")
public class LocalFileUploadServiceImpl implements FileUploadService {

    private final Path baseDir;

    public LocalFileUploadServiceImpl(@Value("${app.upload.base-dir}") String baseDir) {
        this.baseDir = Path.of(baseDir).toAbsolutePath().normalize();
    }

    @Override
    public UploadedFile store(MultipartFile file, String folder) {
        try {
            String safeFolder = sanitize(folder);
            Path targetDir = baseDir.resolve(safeFolder).normalize();
            if (!targetDir.startsWith(baseDir)) {
                throw new IllegalArgumentException("Invalid folder: " + folder);
            }
            Files.createDirectories(targetDir);

            String originalFilename = sanitize(file.getOriginalFilename());
            String storedName = UUID.randomUUID() + "_" + originalFilename;
            Path targetPath = targetDir.resolve(storedName);

            file.transferTo(targetPath);

            String storageKey = safeFolder + "/" + storedName;
            log.debug("Stored file at {}", storageKey);

            return new UploadedFile(storageKey, originalFilename, file.getContentType(), file.getSize());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store file", e);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            Path targetPath = baseDir.resolve(storageKey).normalize();
            if (!targetPath.startsWith(baseDir)) {
                throw new IllegalArgumentException("Invalid storage key: " + storageKey);
            }
            Files.deleteIfExists(targetPath);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete file: " + storageKey, e);
        }
    }

    private String sanitize(String name) {
        if (name == null || name.isBlank()) {
            return "file";
        }
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
