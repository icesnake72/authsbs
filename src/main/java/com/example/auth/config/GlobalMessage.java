package com.example.auth.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Data
@NoArgsConstructor
public class GlobalMessage {
    // 로그인 관련 메시지
    // 성공 메시지
    public static final String LOGIN_SUCCESS = "로그인 성공";

    // 실패
    public static final String PASSWORD_MISMATCH = "패스워드가 일치하지 않습니다.";
}
