package com.streamingService.videoUploading.controller;

public class UploadResponse {

    private String status;
    private String message;

    public UploadResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
