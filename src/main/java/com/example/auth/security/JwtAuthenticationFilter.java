package com.example.auth.security;

import ch.qos.logback.core.util.StringUtil;
import com.example.auth.entity.User;
import com.example.auth.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        System.out.println("JwtAuthenticationFilter::doFilterInternal()");
//        log.debug("JwtAuthenticationFilter::doFilterInternal()");

        try {
            // request header에서 토큰 추출하기
            String token = extractTokenRequest(request);
            System.out.println(token);
            if (token != null) {
                try {
                    if (jwtTokenProvider.validateToken(token)) {
                        System.out.println(token);
                        // 토큰이 일단 유효하다면...
                        String email = jwtTokenProvider.getEmailFromToken(token);
                        Long userId = jwtTokenProvider.getUserIdFromToken(token);

                        // 토큰 정보를 활용하여 사용자 객체를 불러옴
                        User user = userRepository.findById(userId).orElse(null);

                        if (user!=null && user.getIsActive()) {
                            // 권한 정보를 생성
                            List<SimpleGrantedAuthority> authorities = List.of(
                                    new SimpleGrantedAuthority(user.getRole().name())
                            );

                            // 인증 토큰 생성
                            UsernamePasswordAuthenticationToken authenticationToken =
                                    new UsernamePasswordAuthenticationToken(user, null, authorities);

                            // 정보 추가
                            authenticationToken.setDetails(
                                    new WebAuthenticationDetailsSource().buildDetails(request)
                            );

                            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                            System.out.println("doFilterInternal 성공!");
                        }
                    }
                } catch (ExpiredJwtException e) {
                    // 이러한 에러를 클라이언트에게 보내면 클라이언트가 refresh요청을 하도록 유도
                    sendErrorResponse(response, HttpStatus.UNAUTHORIZED);
                } catch (JwtException e) {
                    // 토큰이 잘못됨
                    sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR);
                } catch (IllegalArgumentException e) {
                    // request가 잘 못 만들어짐
                    sendErrorResponse(response, HttpStatus.BAD_REQUEST);
                }
            }
        } catch (Exception e) {
            // log 출력
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7); // Bearer 이후의 토큰 문자열만 반환
        }

        return null;
    }

    private void sendErrorResponse(
            HttpServletResponse response,
            HttpStatus status
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format("{\"Error\" : %d}", status.value());
        response.getWriter().write(jsonResponse);
    }
}
