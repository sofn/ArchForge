package com.lesofn.archsmith.infrastructure.file;

import java.io.InputStream;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * S3 文件存储实现（兼容 MinIO、AWS S3 等）
 *
 * @author sofn
 */
@Slf4j
public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;
    private final String bucket;

    public S3FileStorageService(
            String endpoint, String accessKey, String secretKey, String bucket, String region) {
        this.bucket = bucket;
        this.s3Client =
                S3Client.builder()
                        .endpointOverride(URI.create(endpoint))
                        .credentialsProvider(
                                StaticCredentialsProvider.create(
                                        AwsBasicCredentials.create(accessKey, secretKey)))
                        .region(Region.of(region))
                        .forcePathStyle(true)
                        .build();
        ensureBucketExists();
    }

    private void ensureBucketExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (Exception e) {
            log.info("Bucket '{}' does not exist, creating...", bucket);
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
        }
    }

    @Override
    public String upload(String path, InputStream inputStream, String contentType, long size) {
        PutObjectRequest request =
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(path)
                        .contentType(contentType)
                        .build();
        s3Client.putObject(request, RequestBody.fromInputStream(inputStream, size));
        return path;
    }

    @Override
    public InputStream download(String path) {
        GetObjectRequest request =
                GetObjectRequest.builder().bucket(bucket).key(path).build();
        return s3Client.getObject(request);
    }

    @Override
    public void delete(String path) {
        DeleteObjectRequest request =
                DeleteObjectRequest.builder().bucket(bucket).key(path).build();
        s3Client.deleteObject(request);
    }

    @Override
    public boolean exists(String path) {
        try {
            HeadObjectRequest request =
                    HeadObjectRequest.builder().bucket(bucket).key(path).build();
            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }
}
