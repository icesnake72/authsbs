package com.example.auth.controller;

import com.example.auth.config.GlobalMessage;
import com.example.auth.dto.*;
import com.example.auth.exception.TokenException;
import com.example.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {
    private final AuthService authService;

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok("Authentication Service is running");
    }

    /*
    * {
    *   "email": "test@.com",
    *   "password": "4568",
    *   "username": ""
    * }
    *
    * */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody RequestSignup requestSignup) {

        ApiResponse<Void> response = authService.signup(requestSignup);

        // 회원 가입 성공 여부를 판단하여 response 코드를 설정하고
        HttpStatusCode statusCode = response.getSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;

        // 응답을 반환한다.
        return ResponseEntity.status(statusCode).body(response);
    }

    @PostMapping("/old_login")
    public ResponseEntity<?> login(@Valid @RequestBody RequestLogin requestLogin) {
        LoginResponse response = authService.login(requestLogin);

        if (!(response.getAccessToken().isEmpty())) {
            return ResponseEntity.status(HttpStatus.OK).body(
                    ApiResponse.success("로그인 성공", response)
            );
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("로그인 실패")
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginEx(
            @Valid @RequestBody RequestLogin requestLogin,
            HttpServletRequest request,
            HttpServletResponse httpResponse
    ) {
        LoginResponse response = authService.loginEx(requestLogin);

        if (!(response.getAccessToken().isEmpty())) {
            // 쿠키 생성
            Cookie refreshTokenCookie = new Cookie("refreshToken", response.getRefreshToken());
            refreshTokenCookie.setHttpOnly(true);   // XSS 공격에 대응, JS에서 읽을 수 없음
            refreshTokenCookie.setSecure(false);    // 개발기간만 false, https를 적용하면 true
            refreshTokenCookie.setPath("/");    // 모든 경로에 쿠키 전송
            httpResponse.addCookie( refreshTokenCookie );

            // LoginResponse 인스턴스에 있는 refresh Token 정보를 삭제한다.
            response.setRefreshToken(null);

            return ResponseEntity.status(HttpStatus.OK).body(
                    ApiResponse.success(GlobalMessage.LOGIN_SUCCESS, response)
            );
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("로그인 실패")
            );
        }
    }

    public String extractRefreshTokenFromBody(TokenRefreshRequest body) {
        if (body == null || body.getRefreshToken() == null || body.getRefreshToken().isBlank())
            return null;

        return body.getRefreshToken();
    }

    public String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();    // 쿠키는 복수개 존재할 수 있음, 따라서 배열로 받음
        if (cookies == null) {
            // 에러를 발생시킴
            return null;
        }

        for(Cookie cookie : cookies) {
            // 쿠키의 이름이 refreshToken 인가???
            if ("refreshToken".equals(cookie.getName())) {
                // 맞다면...
                return cookie.getValue();
            }
        }

        return null;
    }

    /**
     *  AccessToken이 만료되면 RefreshToken을 이용하여 AccessToken을 재발급하는 요청 처리
     * */
    @PostMapping("/refresh")
    @SuppressWarnings("NullableProblems")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refresh(
            HttpServletRequest request,     // web 용
            @RequestBody(required = false) @Valid TokenRefreshRequest body  // mobile
    ) {
        //
        // String refreshToken = extractRefreshTokenFromBody( body );
        String refreshToken = extractRefreshTokenFromCookie( request );
        if ( refreshToken == null ) {
            // exception 발생시킴
            throw new TokenException("리프레시 토큰을 읽을 수 없거나, 유효하지 않습니다.");
        }

        // AccessToken을 재발급한 Response 객체를 반환받음
        TokenRefreshResponse tokenRefreshResponse = authService.refreshAccessToken(refreshToken);
        tokenRefreshResponse.setRefreshToken(null);

        return ResponseEntity.ok(
                ApiResponse.success("Access Token 재발급 성공", tokenRefreshResponse)
        );
    }
}
