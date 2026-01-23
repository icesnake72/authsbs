package com.example.auth.controller;

import com.example.auth.dto.ApiResponse;
import com.example.auth.dto.UserProfileResponse;
import com.example.auth.dto.UserProfileUpdateRequest;
import com.example.auth.entity.User;
import com.example.auth.entity.UserProfile;
import com.example.auth.exception.InvalidCredentialException;
import com.example.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

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

    /*
    * 사용자 프로필을 조회요청
    * */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> profile(
            @AuthenticationPrincipal User user
    ) {
        System.out.println("사용자 프로필 조회 요청: " + user.getEmail());

        try {
            UserProfileResponse userProfileResponse = userService.getUserProfile(user.getId());

            ApiResponse<UserProfileResponse> apiResponse =
                    ApiResponse.success("프로필 조회 성공", userProfileResponse);

            return ResponseEntity.ok(apiResponse);

        } catch (InvalidCredentialException ex) {
            return ResponseEntity.ok(ApiResponse.error("프로필 조회 실패"));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody UserProfileUpdateRequest request
    ) {
        UserProfileResponse response = userService.updateUserProfile(user.getId(), request);
        ApiResponse<UserProfileResponse> apiResponse =
                ApiResponse.success("사용자 프로필을 성공적으로 수정하였습니다", response);

        // json response
        return ResponseEntity.ok(apiResponse);
    }
}
