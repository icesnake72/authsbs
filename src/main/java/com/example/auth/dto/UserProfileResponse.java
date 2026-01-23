package com.example.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    // User Table의 정보
    private Long userId;
    private String email;
    private String name;    // nick name;
    private String profileImage; // 프로필 이미지 URL
    private String provider;

    // UserProfile Table의 정보
    private Long userProfileId;
    private String lastName;
    private String firstName;
    private String phoneNumber;
    private String address1;
    private String address2;
    private String bgImage;
    private LocalDateTime createdAt;
}
