package com.boltblazers.rkbrothers.core.upload;

public record UploadedFile(
        String storageKey,
        String originalFilename,
        String contentType,
        long sizeBytes
) {
}
