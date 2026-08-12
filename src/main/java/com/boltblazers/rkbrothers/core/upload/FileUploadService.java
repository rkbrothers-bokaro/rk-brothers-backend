package com.boltblazers.rkbrothers.core.upload;

import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction over file storage. The local filesystem implementation is a
 * stub for development; production deployments can swap in a cloud-backed
 * implementation (e.g. Google Cloud Storage) without touching callers.
 */
public interface FileUploadService {

    UploadedFile store(MultipartFile file, String folder);

    void delete(String storageKey);
}
