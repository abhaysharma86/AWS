package com.codemaker.awsS3Bucket.controller;

import com.codemaker.awsS3Bucket.config.Loggers;
import com.codemaker.awsS3Bucket.services.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
public class S3Controller {
    private final FileUploadService fileUploadService;

    @Autowired
    private Loggers loggers;

    public S3Controller(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {

        loggers.log(1,"Request received for file upload");

        try {
            String fileName = fileUploadService.uploadFile(file);
            return ResponseEntity.ok("Uploaded: " + fileName);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileName) {
        loggers.log(1, String.format("Request received for download file ::: %s : ", fileName));
        try {
            byte[] fileBytes = fileUploadService.downloadFile(fileName);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment filename=" + fileName)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(fileBytes);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Download failed: " + e.getMessage()).getBytes());
        }
    }

    @DeleteMapping("/{file}")
    public ResponseEntity<String> deleteFile(@PathVariable("file") String file) {
        loggers.log(1, String.format("Request received for delete file ::: %s:", file));
        fileUploadService.deleteFile(file);
        return ResponseEntity.ok("File is deleted successfully from S3 bucket");
    }

    @GetMapping("/")
    public List<String> getFile() {
        loggers.log(1, String.format("Request received for retrieving all file name"));
        List<String> strings = fileUploadService.listFiles();
        return strings;
    }


    @GetMapping("/files/{key}/presign")
    public String getPresignedUrl(@PathVariable String key) {
        loggers.log(1, String.format("Request received for generating presign url file name ::: %s: ", key));
        return fileUploadService.generatePresignedUrl(key);
    }


    @GetMapping("/files/{key}/cdnLink")
    public String getCDNLink(@PathVariable String key) {
        loggers.log(1, String.format("Request received for generating CDN link file name ::: %s: ", key));
        return fileUploadService.cdnLink(key);
    }


}
