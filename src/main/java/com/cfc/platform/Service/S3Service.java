package com.cfc.platform.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {

    private static final Logger log = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region:ap-south-1}")
    private String region;

    /**
     * CloudFront domain without protocol or trailing slash.
     * Example: d1abc123xyz.cloudfront.net
     * If blank, the service falls back to a direct S3 URL.
     */
    @Value("${aws.cloudfront.domain:}")
    private String cloudfrontDomain;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Upload a file to the "uploads/" prefix in S3 and return its public URL.
     *
     * @param file the multipart file from the HTTP request
     * @return CloudFront URL  (preferred)  or  direct S3 URL as fallback
     */
    public String uploadFile(MultipartFile file) throws IOException {
        // Build a unique key so uploads never overwrite each other
        String originalName = file.getOriginalFilename() != null
                ? file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_")
                : "file";
        String key = "uploads/" + UUID.randomUUID() + "_" + originalName;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
        log.info("Uploaded {} to s3://{}/{}", originalName, bucketName, key);

        return buildPublicUrl(key);
    }

    /**
     * Delete a file from S3 using its object key (not the full URL).
     * Extract the key from the URL first with {@link #extractKey(String)}.
     */
    public void deleteFile(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
        log.info("Deleted s3://{}/{}", bucketName, key);
    }

    /**
     * Extract the S3 object key from either a CloudFront or direct S3 URL.
     * e.g. "https://d1abc.cloudfront.net/uploads/abc_img.png" → "uploads/abc_img.png"
     */
    public String extractKey(String url) {
        if (url == null) return null;
        // Strip protocol + domain — key starts after the third "/"
        return url.replaceFirst("https?://[^/]+/", "");
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private String buildPublicUrl(String key) {
        if (cloudfrontDomain != null && !cloudfrontDomain.isBlank()) {
            return "https://" + cloudfrontDomain + "/" + key;
        }
        // Fallback: direct S3 URL (only works if bucket has public-read policy)
        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
    }
}
