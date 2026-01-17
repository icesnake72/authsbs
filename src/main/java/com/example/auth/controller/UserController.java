package com.example.auth.controller;

import com.example.auth.dto.ApiResponse;
import com.example.auth.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> me(
            @AuthenticationPrincipal User user      // 로그인한 사용자의 엔티티
    ) {
        // 로그인 사용자의 테스트 url

        System.out.println("현재 사용자 정보 요청: " + user.getEmail());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("email", user.getEmail());
        userInfo.put("name", user.getNickName());
        userInfo.put("profileImage", user.getProfileImage());
        userInfo.put("provider", user.getProvider());
        userInfo.put("role", user.getRole());
        userInfo.put("createdAt", user.getCreatedAt());

        ApiResponse<Map<String, Object>> response =
                ApiResponse.success("사용자 정보 조회 성공", userInfo);

        return ResponseEntity.ok(response);
    }
}
