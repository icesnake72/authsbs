package com.example.auth.dto.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class KakaoUserResponse {

    private Long id;

    @JsonProperty("connected_at")
    private String connectedAt;

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @JsonProperty("properties")
    private Properties properties;


    @Data
    public static class KakaoAccount {

        @JsonProperty("profile_nickname_needs_agreement")
        private Boolean profileNicknameNeedsAgreement;

        @JsonProperty("profile_image_needs_agreement")
        private Boolean profileImageNeedsAgreement;

        private Profile profile;

        @JsonProperty("name_needs_agreement")
        private Boolean nameNeedsAgreement;

        private String name;

        @JsonProperty("email_needs_agreement")
        private Boolean emailNeedsAgreement;

        @JsonProperty("is_email_valid")
        private Boolean isEmailValid;

        @JsonProperty("is_email_verified")
        private Boolean isEmailVerified;

        private String email;

        @JsonProperty("age_range_needs_agreement")
        private Boolean ageRangeNeedsAgreement;

        @JsonProperty("age_range")
        private String ageRange;

        @JsonProperty("birthyear_needs_agreement")
        private Boolean birthyearNeedsAgreement;

        private String birthyear;

        @JsonProperty("birthday_needs_agreement")
        private Boolean birthdayNeedsAgreement;

        private String birthday;

        @JsonProperty("birthday_type")
        private String birthdayType;

        @JsonProperty("is_leap_month")
        private Boolean isLeapMonth;

        @JsonProperty("gender_needs_agreement")
        private Boolean genderNeedsAgreement;

        private String gender;

        @JsonProperty("phone_number_needs_agreement")
        private Boolean phoneNumberNeedsAgreement;

        @JsonProperty("phone_number")
        private String phoneNumber;

        @JsonProperty("ci_needs_agreement")
        private Boolean ciNeedsAgreement;

        private String ci;

        @JsonProperty("ci_authenticated_at")
        private String ciAuthenticatedAt;
    }

    @Data
    public static class Profile {

        private String nickname;

        @JsonProperty("thumbnail_image_url")
        private String thumbnailImageUrl;

        @JsonProperty("profile_image_url")
        private String profileImageUrl;

        @JsonProperty("is_default_image")
        private Boolean isDefaultImage;

        @JsonProperty("is_default_nickname")
        private Boolean isDefaultNickname;
    }

    @Data
    public static class Properties {
        @JsonProperty("nickname")
        private String nickname;

        @JsonProperty("profile_image")
        private String profileImage;

        @JsonProperty("thumbnail_image")
        private String thumbnailImage;
    }
}
