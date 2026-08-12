package com.boltblazers.rkbrothers.core.upload;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Placeholder production implementation. Wire up the GCS client and set
 * GCS_BUCKET once cloud storage is provisioned for this environment.
 */
@Service
@Profile("prod")
public class GcsFileUploadServiceImpl implements FileUploadService {

    @Override
    public UploadedFile store(MultipartFile file, String folder) {
        throw new UnsupportedOperationException("GCS not configured — set GCS_BUCKET env var");
    }

    @Override
    public void delete(String storageKey) {
        throw new UnsupportedOperationException("GCS not configured — set GCS_BUCKET env var");
    }
}
