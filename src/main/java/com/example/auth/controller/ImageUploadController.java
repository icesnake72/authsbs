package com.example.auth.controller;

import com.example.auth.dto.ApiResponse;
import com.example.auth.dto.ImageUploadResponse;
import com.example.auth.entity.User;
import com.example.auth.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class ImageUploadController {
    private final ImageStorageService imageStorageService;

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadImage(
            @AuthenticationPrincipal User user,
            @RequestParam("file")MultipartFile file
    ) {
        // service 일시키기
        log.info("이미지 업로드 요청됨 - userId: {}, 파일명: {}", user.getId(), file.getOriginalFilename());
        ImageUploadResponse response = imageStorageService.store(file);

        return ResponseEntity.ok(ApiResponse.success("이미지 업로드 성공", response));
    }

    // 선택적 옵션 사항
    @DeleteMapping("/image/{fileName}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @AuthenticationPrincipal User user,
            @PathVariable String fileName
    ) {
        imageStorageService.delete(fileName);

        return ResponseEntity.ok(ApiResponse.success("이미지 삭제됨", null));
    }


}
