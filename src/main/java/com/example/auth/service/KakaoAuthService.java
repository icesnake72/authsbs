package com.example.auth.service;

import com.example.auth.config.KakaoOAuthProperties;
import com.example.auth.dto.LoginResponse;
import com.example.auth.dto.kakao.KakaoTokenResponse;
import com.example.auth.dto.kakao.KakaoUserResponse;
import com.example.auth.entity.RefreshToken;
import com.example.auth.entity.User;
import com.example.auth.repository.RefreshTokenRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoAuthService {
    private final KakaoOAuthProperties kakaoOAuthProperties;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper;

    @Value("${oauth.kakao.provider_name}")
    private String PROVIDER_NAME;


    /**
     * 카카오 인가 코드 요청을 보낼 URL 생성
     * 사용자를 카카오 로그인 페이지로 리다이렉션하기 위한 URL
     *
     * @return 카카오 인가 코드 요청 url
     * */
    public String getAuthorizationUrl() {
        String url = UriComponentsBuilder
                .fromUriString(kakaoOAuthProperties.getAuthorizationUri())
                .queryParam("client_id", kakaoOAuthProperties.getClientId())
                .queryParam("redirect_uri", kakaoOAuthProperties.getRedirectUri())
                .queryParam("response_type", "code")
                .build()
                .toUriString();

        System.out.println( url );
        // log.debug("카카오 인증 URL: {}", url);

        return url;
    }

    public KakaoTokenResponse getAccessToken(String code) {
        // kakao로부터 받은 code를 이용하여 access token을 요청함

        // 요청 파라미터를 구성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoOAuthProperties.getClientId());
        params.add("redirect_uri", kakaoOAuthProperties.getRedirectUri());
        params.add("code", code);
        params.add("client_secret", kakaoOAuthProperties.getClientSecret());

        String tokenUrl = kakaoOAuthProperties.getTokenUri();
        KakaoTokenResponse kakaoTokenResponse = restClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.parseMediaType("application/x-www-form-urlencoded;charset=utf-8"))
                .body(params)
                .retrieve()
                .body(KakaoTokenResponse.class);

        try{
            String resJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(kakaoTokenResponse);

            System.out.println("=======================================");
            System.out.println( resJson );
            System.out.println("=======================================");
        } catch (Exception e) {
            System.out.println( e.getMessage() );
        }

        return kakaoTokenResponse;
    }

    public KakaoUserResponse getUserInfo(String accessToken) {
        String userInfoUrl = kakaoOAuthProperties.getUserInfoUri();

        // 카카오 사용자 정보 API호출
        KakaoUserResponse kakaoUserResponse = restClient.get()
                .uri(userInfoUrl)
                .header("Authorization", "bearer "+accessToken)
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .body(KakaoUserResponse.class);

        // Debug
        try{
            String resJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(kakaoUserResponse);

            System.out.println("=======================================");
            System.out.println( resJson );
            System.out.println("=======================================");
        } catch (Exception e) {
            System.out.println( e.getMessage() );
        }
        //

        return kakaoUserResponse;
    }


    public LoginResponse loginByKakao(KakaoUserResponse kakaoUserResponse) {
        String providerId = kakaoUserResponse.getId().toString();
        String email = kakaoUserResponse.getKakaoAccount().getEmail();
        String nickname = kakaoUserResponse.getKakaoAccount().getProfile().getNickname();
        String profileImage = kakaoUserResponse.getKakaoAccount().getProfile().getProfileImageUrl();

        // 이메일 필수!!
        if (email == null || email.isBlank() ) {
            // 이메일을 필수로 처리하지 않는다면 provider와 provider_id를 조합하여 가짜 이메일을 생성한다.
            // 예를들어 KAKAO_xxxxxxxxxx@example.com 을 만들어 email로 활용하도록...

            throw new IllegalArgumentException("카카오 계정의 이메일 정보가 필요합니다");
        }

        User user;
        Optional<User> existingUser = userRepository.findByProviderAndProviderId(PROVIDER_NAME, providerId);
        if (existingUser.isPresent()) {
            // 이미 데이터베이스에 저장된 사용자라면...
            user = existingUser.get();

            // 카카오에서 프로필 정보 업데이트한 내용을 우리 DB에 반영
            user.setNickName(nickname);
            user.setProfileImage(profileImage);
            userRepository.save(user);
        } else {
            // 데이터베이스에 저장되지 않은 사용자라면...
            user = User.builder()
                    .email(email)
                    .nickName(nickname)
                    .password(null)
                    .provider(PROVIDER_NAME)
                    .providerId(providerId)
                    .profileImage(profileImage)
                    .role(User.Role.ROLE_USER)
                    .isActive(true)
                    .build();

            userRepository.save(user);
        }

        // JWT 토큰 생성
        // Refresh Token DB저장
        // LoginResponse 인스턴스 생성하여 반환
        return createLoginResponse(user);
    }


    private LoginResponse createLoginResponse(User user) {
        //
        // 토큰을 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail(), user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        // refresh token은 데이터베이스에 저장한다.
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.ofInstant(
                        jwtTokenProvider.getRefreshTokenExpiryDate().toInstant(),
                        ZoneId.systemDefault()
                )).build();

        refreshTokenRepository.save(refreshTokenEntity);

        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getNickName())
                .provider(user.getProvider())
                .role(user.getRole().toString())
                .build();

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userInfo)
                .build();
    }

}
