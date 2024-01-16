package com.streamingService.videoUploading.controller;

import com.streamingService.videoUploading.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/video")
public class VideoController {

    @Autowired
    private StorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            return storageService.upload(file);
        } catch (Exception e) {
            return new ResponseEntity<>(new UploadResponse("error", "Invalid request format. Please provide a valid file."), HttpStatus.BAD_REQUEST);
        }
    }
}
