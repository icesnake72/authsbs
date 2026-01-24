package com.example.auth.service;

import com.example.auth.dto.ImageUploadResponse;
import com.example.auth.exception.FileStorageException;
import com.example.auth.exception.InvalidFileException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Primary    // (인터페이스)구현체가 여러개 있을 경우 이 인스턴스를 우선적으로 스프링 프레임워크가 주입시켜준다.
public class LocalImageStorageService implements ImageStorageService {
    // 허용할 MIME TYPE 을 정의
    private static final List<String> ALLOWED_IMAGE_TYPE = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    // 최대 파일 크기 설정
    private static final long MAX_FILE_SIZE = 1024 * 1024 * 2;  // 2MB까지 허용

    // 업로드된 파일이 저장될 디렉토리 경로
    private final Path uploadPath;

    // 파일에 접근할 수 있는 기본 Base URL
    private final String baseUrl;


    public LocalImageStorageService(
            @Value("${file.upload.dir}")String uploadDir,
            @Value("${file.upload.base-url}")String baseUrl
    ) throws FileStorageException {
        // 패스(경로)문자열을 받아서 Path 객체로 생성함
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.baseUrl = baseUrl; // 이미지 파일을 서비스할 기본 base url을 설정

        try {
            // 업로드 디렉토리가 없으면 생성해야 한다.
            Files.createDirectories(this.uploadPath);
        } catch (IOException ex) {
            log.error("업로드 디렉토리 생성 실패: {}", ex.getMessage());
            throw new FileStorageException("업로드 디렉토리 생성 실패!!!", ex);
        }

    }



    @Override
    public ImageUploadResponse store(MultipartFile file) {
        log.info("이미지 업로드 store() - 파일명: {}, 크기: {} bytes",
                file.getOriginalFilename(), file.getSize());

        String originalFileName = "";
        String fileExtension = "";
        String fileName = "";
        String imageUrl = "";

        // 파일 검증
        try {
            validateFile(file);

            originalFileName = file.getOriginalFilename();
            fileExtension = getFileExtension(originalFileName);
            fileName = UUID.randomUUID().toString() + fileExtension;

            // 파일 저장 시작
            Path targetPath = this.uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("이미지 파일 저장 완료 - 파일명: {}, 경로: {}", fileName, targetPath);

            // 이미지 URL 생성
            imageUrl = baseUrl + "/" + fileName;

        } catch (IOException e) {
            throw new FileStorageException("이미지 저장 실패: ", e);
        }

        // ImageUploadResponse 객체를 생성하여 반환
        return ImageUploadResponse.builder()
                .imageUrl(imageUrl)
                .fileName(fileName)
                .originalFileName(originalFileName)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();
    }

    @Override
    public void delete(String fileName) {
        try {
            Path filePath = this.uploadPath.resolve(fileName).normalize();

            // 설정된 디렉토리 외부의 파일을 삭제 시도하는 경우
            if (!filePath.startsWith(this.uploadPath)) {
                throw new InvalidFileException("잘 못된 파일 경로입니다.");
            }

            // 파일 삭제 시도
            Files.deleteIfExists(filePath);
            log.info("이미지 파일 삭제 완료 - 파일명: {}", fileName);

        } catch (IOException e) {
            throw new FileStorageException("이미지 삭제중에 오류가 발생했습니다", e);
        }
    }

    private void validateFile(MultipartFile file) throws InvalidFileException {
        // null check
        if (file==null || file.isEmpty()) {
            throw new InvalidFileException("파일이 비어있습니다.");
        }

        // 파일 크기 검증
        if (file.getSize() > MAX_FILE_SIZE ) {
            throw new InvalidFileException("파일의 최대 허용 크기는 2MB입니다.");
        }

        // 지원 MIME TYPE 검증
        String contentType = file.getContentType();
        if (contentType==null || !ALLOWED_IMAGE_TYPE.contains(contentType.toLowerCase())) {
            throw new InvalidFileException("지원하지 않는 파일 형식입니다.");
        }

        // 파일명 검증
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.contains("..")) {
            throw new InvalidFileException("잘 못 된 파일명입니다.");
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf(".");   // "aa.jpg"
        if (lastDotIndex > 0 && lastDotIndex < fileName.length()-1) {
            return fileName.substring(lastDotIndex);
        }

        return "";
    }
}
