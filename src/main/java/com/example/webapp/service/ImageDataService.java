package com.example.webapp.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface ImageDataService {

    public ResponseEntity<Object> uploadFileTos3bucket(MultipartFile multipartFile, String productIdStr, String username);

    public ResponseEntity<Object> getImageById(String productIdStr, String username, String imageIdStr);

    public ResponseEntity<Object> getImagesByProductId(String productIdStr, String username);

    public ResponseEntity<Object> deleteImageById(String productIdStr, String username, String imageIdStr);
}
