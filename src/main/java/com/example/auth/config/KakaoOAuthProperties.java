package com.example.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 카카오 OAuth 인가받을 주용 설정 항목들 저장
 * application.yaml의 oauth.kakao 설정 바인딩(binding)
 * */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "oauth.kakao")
public class KakaoOAuthProperties {

    // 카카오 REST API 키
    private String clientId;

    // 카카오 Client Secret 키
    private String clientSecret;

    // 카카오 인증후 백앤드에 redirect되어질 URL
    private String redirectUri;

    // 카카오 인가 코드 요청 URL
    private String authorizationUri;

    // 카카오 억세스 토큰 요청 URL
    private String tokenUri;

    // 카카오 사용자 정보 조회 URL
    private String userInfoUri;
}
