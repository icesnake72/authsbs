package com.example.auth.controller;

import com.example.auth.config.GlobalMessage;
import com.example.auth.dto.ApiResponse;
import com.example.auth.dto.LoginResponse;
import com.example.auth.dto.kakao.KakaoTokenResponse;
import com.example.auth.dto.kakao.KakaoUserResponse;
import com.example.auth.exception.TokenException;
import com.example.auth.service.KakaoAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth/kakao")
public class KakaoAuthController {
    private final KakaoAuthService kakaoAuthService;

    /**
     * 카카오 로그인 시작 라우트(Route)
     * 사용자를 카카오 로그인 페이지로 리다이렉트 시킴
     * */
    @GetMapping("/login")   // http://localhost:8070/oauth/kakao/login
    public void kakaoLogin(
            @RequestParam(required = false) String redirectUrl,     // 카카오 인증후 front-end로 정보를 최종적으로 전달할 url
            HttpSession session,
            HttpServletResponse response
    ) throws IOException {
        System.out.println("Front-End Callback URL: " + redirectUrl);

        // front-end로부터 전달 받은 redirectUrl이 정상적으로 들어가 있으면...
        if (redirectUrl != null && !redirectUrl.isBlank()) {
            session.setAttribute("kakaoRedirectUrl", redirectUrl);
        } else {
            System.out.println("기본 설정된 front-end redirectUrl을 사용합니다.");
        }


        String authorizationUrl = kakaoAuthService.getAuthorizationUrl();

        response.sendRedirect(authorizationUrl);
    }

    // http://localhost:8070/oauth/kakao/callback
    @GetMapping("/callback")    // http://localhost:8070/oauth/kakao/callback
//    public ResponseEntity<ApiResponse<LoginResponse>> kakaoCallback(
    public void kakaoCallback(
            @RequestParam String code,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) throws IOException {

        System.out.println("code from kakao : " + code);
        try {

            // 카카오 인가 서버에게 access token을 발급받으러 token uri 다시 호출
            KakaoTokenResponse kakaoTokenResponse = kakaoAuthService.getAccessToken(code);

            KakaoUserResponse kakaoUserResponse = kakaoAuthService.getUserInfo(kakaoTokenResponse.getAccessToken());

            // 데이터베이스에 조회하고 없으면 저장
            LoginResponse response = kakaoAuthService.loginByKakao(kakaoUserResponse);

            // Refresh Token cookie설정
//            Cookie refreshTokenCookie = new Cookie("refreshToken", response.getRefreshToken());
//            refreshTokenCookie.setHttpOnly(true);   // XSS 공격에 대응, JS
//            refreshTokenCookie.setSecure(false);    // 개발기간만 false, https를 적용하면 true
//            refreshTokenCookie.setPath("/");    // 모든 경로에 쿠키 전송
//            httpResponse.addCookie(refreshTokenCookie);

            // LoginResponse 인스턴스에 있는 refresh Token 정보를 삭제한다.
//            response.setRefreshToken(null);

            // 세션 정보를 로딩
            HttpSession session = httpRequest.getSession(true);
            // KakaoLoginResponse 인스턴스를 session에 임시적으로 저장해둔다.
            session.setAttribute("pendingLoginResponse", response);

            // frontEndRedirectUrl(프론트엔드 redirect URL 저장한거 불러오기
            HttpSession savedSession = httpRequest.getSession(false);
            String frontEndRedirectUrl = (String)savedSession.getAttribute("kakaoRedirectUrl");
            //============================================================================
            System.out.println("Recovered front end redirect url: " + frontEndRedirectUrl);
            //============================================================================

            // 프론트엔드로 리다이렉트 시키기(status=success로 전달)
            String successRediretUrl = String.format("%s?status=success", frontEndRedirectUrl);

            // front-end로 redirect 시키기
            httpResponse.sendRedirect(successRediretUrl);

//            return ResponseEntity.status(HttpStatus.OK).body(
//                    ApiResponse.success(GlobalMessage.LOGIN_SUCCESS, response)
//            );

        } catch (Exception e) {
            // 카카오 로그인 실패
            System.out.println("Failed to kakao login");

            HttpSession savedSession = httpRequest.getSession(false);
            String frontEndRedirectUrl = (String)savedSession.getAttribute("kakaoRedirectUrl");
            //============================================================================
            System.out.println("Recovered front end redirect url: " + frontEndRedirectUrl);
            //============================================================================

            // 프론트엔드로 리다이렉트 시키기(status=failed로 전달)
            String failedRediretUrl = String.format("%s?status=failed&message=%s", frontEndRedirectUrl, e.getMessage());

            // front-end로 redirect 시키기
            httpResponse.sendRedirect(failedRediretUrl);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//                    ApiResponse.error("카카오 로그인 실패: " + e.getMessage())
//            );
        }
    }


    @PostMapping("/exchange-token")
    public ResponseEntity<ApiResponse<LoginResponse>> exchangeToken(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        System.out.println("/exchange-token called from Front-End");

        // 세션에 임시로 저장해둔 LoginResponse를 복원함
        HttpSession session = httpRequest.getSession(false);
        if (session==null) {
            //
            throw new TokenException("세션이 일치하지 않습니다.");
        }

        LoginResponse response = (LoginResponse)session.getAttribute("pendingLoginResponse");
        if (response==null) {
            //
            throw new TokenException("저장된 LoginResponse가 없습니다.");
        }

        session.removeAttribute("pendingLoginResponse");

        // Refresh Token cookie설정
        Cookie refreshTokenCookie = new Cookie("refreshToken", response.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);   // XSS 공격에 대응, JS
        refreshTokenCookie.setSecure(false);    // 개발기간만 false, https를 적용하면 true
        refreshTokenCookie.setPath("/");    // 모든 경로에 쿠키 전송
        httpResponse.addCookie(refreshTokenCookie);

        // LoginResponse 인스턴스에 있는 refresh Token 정보를 삭제한다.
        response.setRefreshToken(null);

        return ResponseEntity.status(HttpStatus.OK).body(
            ApiResponse.success(GlobalMessage.LOGIN_SUCCESS, response)
        );
    }

}
