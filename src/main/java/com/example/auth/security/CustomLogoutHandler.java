package com.example.auth.security;

import com.example.auth.repository.RefreshTokenRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;


    @Override
    @Transactional
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            @Nullable Authentication authentication
    ) {
        // AccessToken을 request로부터 추출
        String accessToken = extractAccessToken(request);
        if ( accessToken == null) {
            log.warn("Access Token없이 로그아웃 처리를 시도했습니다");
            return;
        }

        // AccessToken을 검증
        if (!jwtTokenProvider.validateToken(accessToken)) {
            log.warn("유효하지 않은 Access Token으로 로그아웃 처리를 시도했습니다.");
            return;
        }

        // RefreshToken을 DB에서 삭제
        String email = jwtTokenProvider.getEmailFromToken(accessToken);
        int deletedCount = refreshTokenRepository.deleteByUserEmail( email );
        System.out.println("Deleted Refresh Tokens : " + deletedCount);

        // 쿠키 삭제
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);    // 즉시만료
        response.addCookie( cookie );
    }

    private String extractAccessToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if ( header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        return null;
    }
}
