package com.example.auth.exception;

import com.example.auth.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리 핸들러
 * 모든 @RestController에서 발생하는 예외처리를 한곳에서 처리할 수 있도록 하는 클래스
 * */
@Slf4j
@RestControllerAdvice   // Spring 전역 예외처리기로 등록
public class GlobalExceptionHandler {
    /**
     * Validation 파라미터 검증 실패시 처리되는 함수
     * RestController에서 @Valid 어노테이션으로 검증 실패했을때 방생하는 예외를 처리함
     *
     * @param ex 검증 실패했을때 발생하는 exception
     * @return 발생했을때의 에러메시지를 response하는 인스턴스
     * */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @SuppressWarnings("NullableProblems")
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("입력값이 올바르지 않습니다.");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.error(errorMessage));
    }



    @ExceptionHandler(TokenException.class)
    @SuppressWarnings("NullableProblems")
    public ResponseEntity<ApiResponse<Void>> handleTokenException(TokenException ex) {
        // logging
        log.warn("토큰 오류 : {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.error(ex.getMessage())
        );
    }


    @ExceptionHandler(AccountException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountException(TokenException ex) {
        // logging
        log.warn("계정 오류 : {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ApiResponse.error(ex.getMessage())
        );
    }

    @ExceptionHandler(DuplicationEmailException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicationEmailException(DuplicationEmailException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.error(ex.getMessage())
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.error(ex.getMessage())
        );
    }

    @ExceptionHandler(InvalidCredentialException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentialException(InvalidCredentialException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.error(ex.getMessage())
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.error("올바르지 않은 Http Method로 요청하였습니다.")
        );
    }
}
