package com.example.auth.service;

import com.example.auth.dto.ImageUploadResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class S3ImageStorageService implements ImageStorageService {
    @Override
    public ImageUploadResponse store(MultipartFile file) {
        return null;
    }

    @Override
    public void delete(String fileName) {
    }
}
