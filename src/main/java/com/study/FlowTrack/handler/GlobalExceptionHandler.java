package com.study.FlowTrack.handler;

import com.study.FlowTrack.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler
    public ResponseEntity<Object> handleValidationExceptions (MethodArgumentNotValidException ex) {
        Map<String,String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<String> handleDuplicateResource(DuplicateResourceException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException ex) {
        // 404: Ресурс не найден
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PermissionDeniedException.class)
    public ResponseEntity<String> handlePermissionDenied(PermissionDeniedException ex) {
        // 403: Запрещено (нет прав)
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<String> handleInvalidToken(InvalidTokenException ex) {
        // 401: Несанкционировано (токен не прошел проверку)
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<String> handleRateLimitExceeded(RateLimitExceededException ex) {
        // 429: Слишком много запросов
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(InitialRoleNotFoundException.class)
    public ResponseEntity<String> handleInitialRoleNotFound(InitialRoleNotFoundException ex) {
        // 500: Внутренняя ошибка сервера
        return new ResponseEntity<>(
                "Critical System Error: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
    @ExceptionHandler(org.springframework.security.core.userdetails.UsernameNotFoundException.class)
    public ResponseEntity<String> handleSpringUsernameNotFound(org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
        // 401: Несанкционировано
        return new ResponseEntity<>("Authentication failed: Invalid credentials.", HttpStatus.UNAUTHORIZED);
    }

}
