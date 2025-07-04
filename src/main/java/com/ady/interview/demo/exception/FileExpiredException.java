package com.ady.interview.demo.exception;

public class FileExpiredException extends RuntimeException {
    public FileExpiredException(String message) {
        super(message);
    }

    public FileExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
