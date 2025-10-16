package org.mentorship.reflectly.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Generic API response wrapper for consistent response format.
 * Used for all API endpoints to maintain consistency.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponseDto<T> {

    private boolean success;
    private T data;
    private String message;
    private ErrorDto error;

    /**
     * Create a successful response with data.
     */
    public static <T> ApiResponseDto<T> success(T data) {
        return ApiResponseDto.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    /**
     * Create a successful response with data and message.
     */
    public static <T> ApiResponseDto<T> success(T data, String message) {
        return ApiResponseDto.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }

    /**
     * Create a successful response with only message.
     */
    public static <T> ApiResponseDto<T> success(String message) {
        return ApiResponseDto.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    /**
     * Create an error response.
     */
    public static <T> ApiResponseDto<T> error(ErrorDto error) {
        return ApiResponseDto.<T>builder()
                .success(false)
                .error(error)
                .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorDto {
        private String code;
        private String message;
        private Object details;
    }
}
