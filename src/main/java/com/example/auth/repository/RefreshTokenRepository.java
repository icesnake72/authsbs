package com.example.auth.repository;

import com.example.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@SuppressWarnings("NullableProblems")
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * 토큰 문자열을 전달하여 RefreshToken Entity를 가져온다
     * @param token Refresh 토큰 문자열
     * @return RefreshToken Entity by Optional(Null safty)
     * */
    Optional<RefreshToken> findByToken(String token);


    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.email = :email")
    int deleteByUserEmail(@Param("email") String email);
}
