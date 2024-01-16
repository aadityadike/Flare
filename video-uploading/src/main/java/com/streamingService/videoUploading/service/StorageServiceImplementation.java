package com.streamingService.videoUploading.service;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.streamingService.videoUploading.controller.UploadResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.UUID;

@Service
public class StorageServiceImplementation implements StorageService {

    private static final String DOWNLOAD_URL_FORMAT = "https://firebasestorage.googleapis.com/v0/b/flare-dev-13da3.appspot.com/o/%s?alt=media";
    private static final String MP4_CONTENT_TYPE = "video/mp4";

    public ResponseEntity<UploadResponse> uploadFile(File file, String fileName) {
        try (InputStream credentialsStream = getClass().getClassLoader().getResourceAsStream("Firebase.json")) {
            assert credentialsStream != null;
            Credentials credentials = GoogleCredentials.fromStream(credentialsStream);
            Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
            BlobId blobId = BlobId.of("flare-dev-13da3.appspot.com", fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(MP4_CONTENT_TYPE).build();
            storage.create(blobInfo, Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(new UploadResponse("error", "Video couldn't upload. Something went wrong: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            file.delete(); 
        }

        String downloadUrl = String.format(DOWNLOAD_URL_FORMAT, URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        return new ResponseEntity<>(new UploadResponse("success", downloadUrl), HttpStatus.OK);
    }

    private File convertToFile(MultipartFile multipartFile, String fileName) throws IOException {
        File tempFile = new File(fileName);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(multipartFile.getBytes());
        }
        return tempFile;
    }

    @Override
    public ResponseEntity<UploadResponse> upload(MultipartFile multipartFile) {
        try {
            if (multipartFile.isEmpty()) {
                throw new MissingServletRequestPartException("file");
            }

            if (!MP4_CONTENT_TYPE.equals(multipartFile.getContentType())) {
                return new ResponseEntity<>(new UploadResponse("error", "Only MP4 files are allowed."), HttpStatus.BAD_REQUEST);
            }

            String originalFilename = Objects.requireNonNull(multipartFile.getOriginalFilename());
            if (originalFilename.isBlank()) {
                return new ResponseEntity<>(new UploadResponse("error", "Please provide a non-empty file."), HttpStatus.BAD_REQUEST);
            }

            String fileName = UUID.randomUUID().toString().concat(this.getExtension(originalFilename));
            File file = this.convertToFile(multipartFile, fileName);
            return this.uploadFile(file, fileName);
        } catch (MissingServletRequestPartException e) {
            return new ResponseEntity<>(new UploadResponse("error", "File is required in the request."), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(new UploadResponse("error", "Video couldn't upload. Something went wrong: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
