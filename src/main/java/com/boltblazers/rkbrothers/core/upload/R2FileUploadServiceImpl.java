package com.boltblazers.rkbrothers.core.upload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.UUID;

@Slf4j
@Primary
@Service
public class R2FileUploadServiceImpl implements FileUploadService {

    @Value("${R2_ENDPOINT:}")
    private String endpoint;
    
    @Value("${R2_ACCESS_KEY:}")
    private String accessKey;
    
    @Value("${R2_SECRET_KEY:}")
    private String secretKey;
    
    @Value("${R2_BUCKET:}")
    private String bucket;

    private S3Client s3Client;

    @PostConstruct
    public void init() {
        if (endpoint == null || endpoint.isBlank()) {
            log.warn("R2_ENDPOINT is not set. Cloudflare R2 file uploads will not work.");
            return;
        }
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of("auto"))
                .forcePathStyle(true)
                .build();
        log.info("Initialized Cloudflare R2 Upload Service for bucket: {}", bucket);
    }

    @Override
    public UploadedFile store(MultipartFile file, String folder) {
        String originalFilename = sanitize(file.getOriginalFilename());
        String storedName = UUID.randomUUID() + "_" + originalFilename;
        String storageKey = (folder == null || folder.isBlank() ? "" : sanitize(folder) + "/") + storedName;

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(storageKey)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.debug("Stored file at R2: {}", storageKey);
            
            return new UploadedFile(storageKey, originalFilename, file.getContentType(), file.getSize());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store file in R2", e);
        }
    }

    @Override
    public void delete(String storageKey) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(storageKey)
                .build();
        s3Client.deleteObject(deleteRequest);
        log.debug("Deleted file from R2: {}", storageKey);
    }

    private String sanitize(String name) {
        if (name == null || name.isBlank()) {
            return "file";
        }
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
