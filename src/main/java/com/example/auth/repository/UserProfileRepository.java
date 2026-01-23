package com.example.auth.repository;

import com.example.auth.entity.User;
import com.example.auth.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    /**
     * 사용자 id(UserProfile의 id가 아님)로 프로필 정보를 조회한다.
     * @param user 사용자 ID(User 테이블의 ID)
     * @return 사용자 프로필 정보
     * */
    Optional<UserProfile> findByUser(User user);


    /**
     * 사용자 id(UserProfile의 id가 아님)로 사용자의 프로필 정보가 존재하는지 확인
     * @param userId 사용자 ID(User 테이블의 ID)
     * @return 사용자 프로필이 존재하면 true, 아니면 false를 반환
     * */
    boolean existsByUser(Long userId);
}
