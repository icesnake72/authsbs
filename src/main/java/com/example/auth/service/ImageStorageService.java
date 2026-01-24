package com.example.auth.service;

import com.example.auth.dto.ImageUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 이미지 저장 서비스 인터페이스
 * 나중에 AWS S3, EBS등 다른 스토리지로 확장 가능하도록 인터페이스로 정의
 * */
public interface ImageStorageService {

    ImageUploadResponse store(MultipartFile file);

    void delete(String fileName);
}
