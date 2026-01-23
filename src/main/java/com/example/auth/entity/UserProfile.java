package com.example.auth.entity;

import com.example.auth.dto.UserProfileResponse;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profile") // 테이블 이름과 매핑
@Getter // 모든 필드에 대한 getter 메서드 자동 생성
@Setter
@NoArgsConstructor // 기본 생성자 자동 생성
@AllArgsConstructor // 매개변수 생성자 자동 생성
@Builder // 빌더 패턴 사용을 위한 어노테이션
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // AUTO_INCREMENT
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  // 연관 관계 설정 (user 테이블의 id와 연결)
    @JoinColumn(name = "user", referencedColumnName = "id", nullable = false)  // 외래 키 설정
    private User user;  // User 엔티티와 연결

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(name = "address1", length = 100)
    private String address1;

    @Column(name = "address2", length = 100)
    private String address2;

    @Column(name = "bg_image", length = 500)
    private String bgImage;

    @CreationTimestamp  // Hibernate가 자동으로 생성시간 설정
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp    // Hibernate가 자동으로 업데이트 시간을 수정해줌
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    public static UserProfileResponse toUserProfileResponse(User user, UserProfile userProfile) {
        UserProfileResponse userProfileResponse = new UserProfileResponse();

        userProfileResponse.setUserId(user.getId());
        userProfileResponse.setEmail(user.getEmail());
        userProfileResponse.setName(user.getNickName());
        userProfileResponse.setProfileImage(user.getProfileImage());
        userProfileResponse.setProvider(user.getProvider());

        userProfileResponse.setUserProfileId(userProfile.getId());
        userProfileResponse.setLastName(userProfile.getLastName());
        userProfileResponse.setFirstName(userProfile.getFirstName());
        userProfileResponse.setPhoneNumber(userProfile.getPhoneNumber());
        userProfileResponse.setAddress1(userProfile.getAddress1());
        userProfileResponse.setAddress2(userProfile.getAddress2());
        userProfileResponse.setBgImage(userProfile.getBgImage());
        userProfileResponse.setCreatedAt(userProfile.getCreatedAt());

        return userProfileResponse;
    }


    public static UserProfileResponse toUserProfileResponse(User user) {
        UserProfileResponse userProfileResponse = new UserProfileResponse();

        userProfileResponse.setUserId(user.getId());
        userProfileResponse.setEmail(user.getEmail());
        userProfileResponse.setName(user.getNickName());
        userProfileResponse.setProfileImage(user.getProfileImage());
        userProfileResponse.setProvider(user.getProvider());

        return userProfileResponse;
    }


}
