package com.example.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Value("${file.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 업로드 디렉토리 절대 경로 생성
        String absoluteUploadPath = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();

        log.info("정적 리소스(URL) 매핑 설정 - /uploads/** ===> {}", absoluteUploadPath);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(absoluteUploadPath);
    }
}
