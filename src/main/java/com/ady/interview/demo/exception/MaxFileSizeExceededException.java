package com.ady.interview.demo.exception;

public class MaxFileSizeExceededException extends RuntimeException {
    public MaxFileSizeExceededException(String message) {
        super(message);
    }

    public MaxFileSizeExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
