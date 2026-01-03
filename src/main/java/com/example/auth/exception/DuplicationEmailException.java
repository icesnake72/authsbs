package com.example.auth.exception;

public class DuplicationEmailException extends RuntimeException {

    public DuplicationEmailException(String message) {
        super(message);
    }

    public DuplicationEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
