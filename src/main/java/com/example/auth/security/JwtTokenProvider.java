package com.example.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        // 문자열 secret key를 SecretKey 인스턴스로 변환(생성)
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }


    /**
     * User의 email과 id를 받아서 AccessToken을 생성하는 메소드
     *
     * @param userEmail 사용자 이메일, userId 데이터베이스에 저장된 사용자 id
     * @return access token (문자열)
     * */
    public String generateAccessToken(String userEmail, Long userId) {
        Date now = new Date();
        // 만료 시간을 현재시간 + accessTokenExpiration으로 설정함
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(userEmail)             // token의 주체(사용자 이메일)
                .claim("userId", userId)      // 사용자 id 추가
                .claim("type", "access")    // type은 access token
                .issuedAt(now)                  // 발행 시간
                .expiration(expiryDate)         // 만료 시간
                .signWith(secretKey)            // 암호화
                .compact();                     // 생성
    }

    /**
     * User의 email를 받아서 RefreshToken을 생성하는 메소드
     *
     * @param userEmail 사용자 이메일
     * @return refresh token (문자열)
     * */
    public String generateRefreshToken(String userEmail) {
        Date now = new Date();
        // 만료 시간을 현재시간 + accessTokenExpiration으로 설정함
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .subject(userEmail)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)  // 서명 검증
                .build()                // 분석객체
                .parseSignedClaims(token)   // 토큰 주입
                .getPayload();          // claim 데이터를 뽑아냄(Claims 인스턴스)
    }

    // email 추출
    public String getEmailFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    // 사용자 id 추출
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 토큰 검증, 토근 분석시에 예외(exception)이 발생하면 유효하지 않음
     *
     * @param token
     * @return token이 유효하면 true, 아니면 false를 반환
     * */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Refresh Token 만료 시간 계산
    public Date getRefreshTokenExpiryDate() {
        return new Date(System.currentTimeMillis() + refreshTokenExpiration);
    }
}
