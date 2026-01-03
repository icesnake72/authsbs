package com.example.auth.service;

import com.example.auth.dto.*;
import com.example.auth.entity.RefreshToken;
import com.example.auth.entity.User;
import com.example.auth.exception.AccountException;
import com.example.auth.exception.DuplicationEmailException;
import com.example.auth.exception.InvalidCredentialException;
import com.example.auth.exception.TokenException;
import com.example.auth.repository.RefreshTokenRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.CustomUserDetails;
import com.example.auth.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;    // token발급기이자 검증기
    private final AuthenticationManager authenticationManager;

    @Transactional
    public ApiResponse<Void> signup(RequestSignup requestSignup) {
        // 이메일 정규화(Normalize)
        String email = requestSignup.getEmail().trim().toLowerCase();

        log.info("request email : {}", email);

        try {
            // requestSignup정보를 기반으로 User Entity 인스턴스를 생성
            User user = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(requestSignup.getPassword()))
                    .nickName(requestSignup.getUsername())
                    .role(User.Role.ROLE_USER)
                    .isActive(true)
                    .build();

            userRepository.save(user);
            return ApiResponse.success("회원 가입 성공");

        } catch (DataIntegrityViolationException e) {
            System.out.println("중복 회원 가입 시도");
            // log.warn("중복 회원 가입 시도: {}", email);
            throw new DuplicationEmailException("이미 가입된 이메일입니다.");

            // return ApiResponse.error("이미 가입된 회원입니다");
        } catch (Exception e) {
            log.error("회원 가입중 오류 발생: {}", e.getMessage());
            return ApiResponse.error("회원가입 중 오류가 발생했습니다");
        }
    }

    @Transactional
    public LoginResponse login(RequestLogin requestLogin) {

        String normalizeEmail = requestLogin.getEmail().trim().toLowerCase();

        // 이메일로 사용자 조회
        Optional<User> optUser = userRepository.findByEmail(normalizeEmail);
        User user = null;
        if (optUser.isPresent())
            user = optUser.get();
        if (user == null) {
            // 존재하지 않는 이메일이면 에러반환 후 종료
            throw new InvalidCredentialException("존재하지 않는 사용자입니다.");
        }

        // 비밀번호 체크하기
        boolean isValid = passwordEncoder.matches(
                requestLogin.getPassword(), user.getPassword()
        );
        if ( !isValid ) {
            throw new InvalidCredentialException("비밀번호가 일치하지 않습니다.");
        }

        // 계정 상태 확인하기
        if (!user.getIsActive()) {
            throw new AccountException("비활성화된 계정입니다.");
        }

        return createLoginResponse(user);
    }


    @Transactional
    public LoginResponse loginEx(@Valid RequestLogin requestLogin) {
        String email = requestLogin.getEmail().trim().toLowerCase();

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, requestLogin.getPassword())
            );
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

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



    private ApiResponse<Void> createErrorResponse(String message) {
        return ApiResponse.error(message);
    }

    public TokenRefreshResponse refreshAccessToken(String refreshToken) {

        // 1. refresh token 검증하기
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new TokenException("유효하지 않은 Refresh Token입니다.");
        }

        // 2. Refresh Token으로부터 이메일 추출하기
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);

        // 3. DB에 해당 사용자가 존재하는지, 해당 refresh token이 존재하는지 확인
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken).orElseThrow(()-> {
                    return new TokenException("유효하지 않은 Refresh Token입니다.");
        });

        // 4. refresh token이 만료되었는지 확인
        if (tokenEntity.getExpiresAt().isBefore((LocalDateTime.now()))) {
            throw new TokenException("Refresh Token이 만료되었습니다");
        }

        // 5. 사용자 조회
        User user = tokenEntity.getUser();
        if (user==null || !user.getIsActive()) {
            throw new AccountException("비활성화된 사용자입니다");
        }

        // 6. email 아이디 체크하기
        if (!user.getEmail().equals(email)) {
            throw new AccountException("잘 못된 사용자입니다");
        }

        // 통과!
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getEmail(), user.getId());

        // 토큰 응답 객체를 생성
        TokenRefreshResponse response = new TokenRefreshResponse();
        response.setAccessToken(newAccessToken);    // 새로 발급받은 Access Token
        response.setRefreshToken(refreshToken);     // Access Token 발급을 위해 사용된 Refresh Token

        return response;
    }
}
