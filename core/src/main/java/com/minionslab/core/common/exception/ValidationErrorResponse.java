package com.minionslab.core.common.exception;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorResponse extends ErrorResponse {
  private Map<String, String> errors;

  public ValidationErrorResponse(int status, String message, LocalDateTime timestamp, Map<String, String> errors) {
    super(status, message, timestamp);
    this.errors = errors;
  }
} 