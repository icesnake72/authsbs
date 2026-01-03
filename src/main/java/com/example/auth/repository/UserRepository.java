package com.example.auth.repository;

import com.example.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@SuppressWarnings("NullableProblems")   // NullPointerException에 관련된 경고를 없앰
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * 이메일 문자열을 받아서 User 인스턴스를 반환한다.
     * @param email 조회할 사용자 email 문자열
     * @return 사용자 정보(Optional로 한번 감쌈, Null Safety Code)
     * */
    Optional<User> findByEmail(String email);

    /**
     * 사용자 이메일 문자열을 받아서 해당 테이블에 있는지 검사하는 함수
     * @param email 조회할 사용자 email 문자열
     * @return 해당 사용자가 있는지 여부를 반환, 있으면 true, 없으면 false
     * */
    boolean existsByEmail(String email);

    /**
     * OAuth Provider(제공자)와 ProviderId(제공자 id)로 사용자를 조회한다.
     * @param provider OAuth 제공자(KAKAO, GOOGLE 등)
     * @param providerId OAuth제공자의 사용자 고유 id
     * @return 사용자 정보 (Optional)
     * */
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
}
