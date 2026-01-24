package com.example.auth.exception;

import java.io.IOException;

public class FileStorageException extends RuntimeException {
    public FileStorageException(String message, IOException ex) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
