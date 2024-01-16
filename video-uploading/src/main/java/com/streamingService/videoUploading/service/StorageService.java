package com.streamingService.videoUploading.service;

import com.streamingService.videoUploading.controller.UploadResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    ResponseEntity<UploadResponse> upload(MultipartFile file);
}
