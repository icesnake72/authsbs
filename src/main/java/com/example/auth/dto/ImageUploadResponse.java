package com.example.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageUploadResponse {

    private String imageUrl;        // 업로된 이미지의 url, 클라이언트에서 이 url로 접근 가능
    private String fileName;        // 저장된 파일명, UUID로 생성됨
    private String originalFileName;    // 원본 파일명
    private Long fileSize;              // 파일 크기
    private String contentType;         // 이미지 타입(jpg, png 등...)
}
