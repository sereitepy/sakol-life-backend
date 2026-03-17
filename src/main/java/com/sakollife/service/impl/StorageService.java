package com.sakollife.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

/**
 * Handles file uploads to Digital Ocean Spaces (S3-compatible).
 *
 * Configuration (add to application.properties or environment):
 *   do.spaces.endpoint=https://<region>.digitaloceanspaces.com
 *   do.spaces.bucket=<your-space-name>
 *   do.spaces.access-key=<your-access-key>
 *   do.spaces.secret-key=<your-secret-key>
 *   do.spaces.cdn-base-url=https://<your-space-name>.<region>.cdn.digitaloceanspaces.com
 *
 * DO Spaces is fully S3-compatible — we use AWS SDK v2 pointed at the DO endpoint.
 */
@Service
@Slf4j
public class StorageService {

    private final S3Client s3Client;
    private final String bucket;
    private final String cdnBaseUrl;

    public StorageService(
            @Value("${do.spaces.endpoint}") String endpoint,
            @Value("${do.spaces.bucket}") String bucket,
            @Value("${do.spaces.access-key}") String accessKey,
            @Value("${do.spaces.secret-key}") String secretKey,
            @Value("${do.spaces.cdn-base-url}") String cdnBaseUrl) {

        this.bucket = bucket;
        this.cdnBaseUrl = cdnBaseUrl.endsWith("/") ? cdnBaseUrl : cdnBaseUrl + "/";

        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                // DO Spaces works with any AWS region string — use the actual DO region
                .region(Region.of("us-east-1"))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    /**
     * Uploads a university banner image to DO Spaces.
     *
     * @param universityId  used to build a stable, predictable object key
     * @param file          the multipart file from the admin request
     * @return              the public CDN URL of the uploaded image
     */
    public String uploadUniversityBanner(UUID universityId, MultipartFile file) {
        validateImageFile(file);

        String extension = getExtension(file.getOriginalFilename());
        // Key format: university-banners/<uuid>.<ext>
        // Using the UUID means re-uploading replaces the old image automatically
        String key = "university-banners/" + universityId + "." + extension;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .acl(ObjectCannedACL.PUBLIC_READ)   // banner is publicly viewable
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            log.info("Uploaded banner for university {} → {}", universityId, key);

            return cdnBaseUrl + key;

        } catch (IOException e) {
            throw new RuntimeException("Failed to read upload file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("DO Spaces upload failed: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes an object from DO Spaces by its full CDN URL.
     * Used when replacing a banner — optional, old key is overwritten anyway
     * if the filename is UUID-based, but useful for explicit cleanup.
     */
    public void deleteByUrl(String cdnUrl) {
        if (cdnUrl == null || !cdnUrl.startsWith(cdnBaseUrl)) return;
        String key = cdnUrl.substring(cdnBaseUrl.length());
        try {
            s3Client.deleteObject(b -> b.bucket(bucket).key(key));
            log.info("Deleted DO Spaces object: {}", key);
        } catch (Exception e) {
            log.warn("Failed to delete DO Spaces object {}: {}", key, e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed. Received: " + contentType);
        }
        // 5 MB max
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File too large. Max size is 5 MB.");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}