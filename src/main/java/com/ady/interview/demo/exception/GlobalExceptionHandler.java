package com.ady.interview.demo.exception;

import com.ady.interview.demo.dto.MessageResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        log.info("ConstraintViolationException: {}", ex.getMessage());

        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(HttpStatus.BAD_REQUEST, errors.toString()));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            FileNotFoundException.class,
    })
    public ResponseEntity<?> HandleGenericException(FileNotFoundException e) {
        log.info("Not Found Exception: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            StorageException.class,
            MaxFileSizeExceededException.class,
            FileExpiredException.class
    })
    public ResponseEntity<?> HandleGenericException(Exception e) {
        log.info("Bad Request Exception: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(HttpStatus.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception e) {
        log.error("Generic Exception: {}", e.getMessage(), e);

        if (e.getCause() instanceof ConstraintViolationException cve) {
            return handleConstraintViolationException(cve);
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(HttpStatus.BAD_REQUEST, "Unexpected error occurred."));
    }

}