package com.codemaker.awsS3Bucket.services;

import com.codemaker.awsS3Bucket.config.Constant;
import com.codemaker.awsS3Bucket.config.EnvConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadService {

    private final S3Client s3Client;
    private  final EnvConfig config;
    private final S3Presigner s3Presigner;

    public FileUploadService(S3Client s3Client, EnvConfig config, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.config = config;
        this.s3Presigner = s3Presigner;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(config.getBucketName())
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return fileName; // or return URL if needed
    }

    public byte[] downloadFile(String key) throws IOException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(config.getBucketName())
                .key(key)
                .build();

        try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest)) {
            return s3Object.readAllBytes();
        }
    }

    public void deleteFile(String fileName) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(config.getBucketName())
                .key(fileName)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    public List<String> listFiles() {
        ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                .bucket(config.getBucketName())
                .build();

        ListObjectsV2Response listRes = s3Client.listObjectsV2(listReq);

        return listRes.contents()
                .stream()
                .map(S3Object::key) // file name
                .toList();
    }

    public String generatePresignedUrl(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(config.getBucketName())
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest)
                .url()
                .toString();
    }

    public String cdnLink(String fileName){
        return config.getEndpoint() + Constant.SLES + config.getBucketName() + Constant.SLES + fileName;
    }


}
