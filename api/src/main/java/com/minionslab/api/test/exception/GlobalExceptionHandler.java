package com.minionslab.api.test.exception;

import com.minionslab.core.common.exception.ErrorResponse;
import com.minionslab.core.common.exception.MinionException;
import com.minionslab.core.common.exception.PromptException;
import com.minionslab.core.common.exception.ValidationErrorResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(PromptException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorResponse> handlePromptException(PromptException ex) {
    log.error("Prompt exception occurred", ex);
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            LocalDateTime.now()
        ));
  }

  @ExceptionHandler(PromptException.PromptNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseEntity<ErrorResponse> handlePromptNotFoundException(PromptException.PromptNotFoundException ex) {
    log.error("Prompt not found", ex);
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            LocalDateTime.now()
        ));
  }

  @ExceptionHandler(MinionException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<ErrorResponse> handleMinionException(MinionException ex) {
    log.error("Minion exception occurred", ex);
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            ex.getMessage(),
            LocalDateTime.now()
        ));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ValidationErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
    log.error("Validation failed", ex);
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(new ValidationErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation failed",
            LocalDateTime.now(),
            errors
        ));
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
    log.error("Access denied", ex);
    return ResponseEntity
        .status(HttpStatus.FORBIDDEN)
        .body(new ErrorResponse(
            HttpStatus.FORBIDDEN.value(),
            "Access denied: " + ex.getMessage(),
            LocalDateTime.now()
        ));
  }

  @ExceptionHandler(AuthenticationException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
    log.error("Authentication failed", ex);
    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(new ErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "Authentication failed: " + ex.getMessage(),
            LocalDateTime.now()
        ));
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
    log.error("Unexpected error occurred", ex);
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred",
            LocalDateTime.now()
        ));
  }
} 