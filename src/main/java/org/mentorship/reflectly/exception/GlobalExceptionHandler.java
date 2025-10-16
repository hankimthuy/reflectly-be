package org.mentorship.reflectly.exception;

import org.mentorship.reflectly.DTO.ApiResponseDto;
import org.mentorship.reflectly.constants.ApiResponseCodes;
import org.mentorship.reflectly.util.HttpStatusUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for handling validation errors and other exceptions.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle validation errors from @Valid annotations.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponseDto.ErrorDto errorDto = ApiResponseDto.ErrorDto.builder()
                .code(ApiResponseCodes.VALIDATION_ERROR)
                .message(ApiResponseCodes.VALIDATION_ERROR_MESSAGE)
                .details(errors)
                .build();

        return ResponseEntity.badRequest().body(ApiResponseDto.error(errorDto));
    }

    /**
     * Handle generic runtime exceptions.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleRuntimeException(RuntimeException ex) {
        ApiResponseDto.ErrorDto errorDto = ApiResponseDto.ErrorDto.builder()
                .code(ApiResponseCodes.INTERNAL_SERVER_ERROR)
                .message(ex.getMessage())
                .details(null)
                .build();

        return ResponseEntity.status(HttpStatusUtils.getDefaultErrorStatus()).body(ApiResponseDto.error(errorDto));
    }

    /**
     * Handle generic exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleGenericException(Exception ex) {
        ApiResponseDto.ErrorDto errorDto = ApiResponseDto.ErrorDto.builder()
                .code(ApiResponseCodes.INTERNAL_SERVER_ERROR)
                .message(ApiResponseCodes.INTERNAL_SERVER_ERROR_MESSAGE)
                .details(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatusUtils.getDefaultErrorStatus()).body(ApiResponseDto.error(errorDto));
    }
}
