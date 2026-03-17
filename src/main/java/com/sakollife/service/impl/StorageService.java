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
                .region(Region.of("us-east-1"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    /** Upload university hero banner. Key: university-banners/<uuid>.<ext> */
    public String uploadUniversityBanner(UUID universityId, MultipartFile file) {
        return upload("university-banners/" + universityId + "." + getExtension(file), file);
    }

    /**
     * Upload a facility photo.
     * Key: facility-photos/<universityId>/<randomUUID>.<ext>
     * Uses a random UUID so multiple photos per university don't overwrite each other.
     */
    public String uploadFacilityPhoto(UUID universityId, MultipartFile file) {
        return upload("facility-photos/" + universityId + "/" + UUID.randomUUID() + "." + getExtension(file), file);
    }

    /** Delete an object by its full CDN URL. */
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

    // ── Internal ──────────────────────────────────────────────────────────────

    private String upload(String key, MultipartFile file) {
        validateImageFile(file);
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket).key(key)
                    .contentType(file.getContentType())
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            log.info("Uploaded to DO Spaces: {}", key);
            return cdnBaseUrl + key;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read upload file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("DO Spaces upload failed: " + e.getMessage(), e);
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("File is empty");
        String ct = file.getContentType();
        if (ct == null || !ct.startsWith("image/"))
            throw new IllegalArgumentException("Only image files allowed. Got: " + ct);
        if (file.getSize() > 5 * 1024 * 1024)
            throw new IllegalArgumentException("File too large. Max 5 MB.");
    }

    private String getExtension(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null || !name.contains(".")) return "jpg";
        return name.substring(name.lastIndexOf('.') + 1).toLowerCase();
    }
}