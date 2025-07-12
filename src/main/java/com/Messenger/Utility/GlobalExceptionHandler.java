package com.Messenger.Utility;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	// called by Spring when @Valid fails in request body
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
		Map<String, String> fieldErrors = new HashMap<>();
		ex.getBindingResult().getFieldErrors()
				.forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));

		CommonUtils.logError(ex);
		return CommonUtils.buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors);
	}

	// called when malformed JSON is received
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, Object>> handleInvalidJson(HttpMessageNotReadableException ex) {
		CommonUtils.logError(ex);
		return CommonUtils.buildErrorResponse(HttpStatus.BAD_REQUEST, "Malformed JSON request", null);
	}

	// called when query/form param constraints fail (e.g., @Min, @NotBlank in
	// query)
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
		Map<String, String> errors = new HashMap<>();
		ex.getConstraintViolations()
				.forEach(violation -> errors.put(violation.getPropertyPath().toString(), violation.getMessage()));

		CommonUtils.logError(ex);
		return CommonUtils.buildErrorResponse(HttpStatus.BAD_REQUEST, "Constraint violation", errors);
	}

	// ✅ Called automatically when you throw a custom AppException manually
	@ExceptionHandler(AppException.class)
	public ResponseEntity<Map<String, Object>> handleAppException(AppException ex) {
		CommonUtils.logError(ex);
		return CommonUtils.buildErrorResponse(ex.getStatus(), ex.getMessage(), null);
	}

	// Catch-all for any uncaught exceptions
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleAll(Exception ex) {
		CommonUtils.logError(ex);
		return CommonUtils.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong", null);
	}

}
