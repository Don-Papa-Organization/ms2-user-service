package com.users.UsuariosYEmpleados.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final String message;
    private final String timestamp;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    .withZone(ZoneOffset.UTC);

    private ApiResponse(boolean success, T data, String message, String timestamp) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.timestamp = timestamp;
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, nowTimestamp());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message, nowTimestamp());
    }

    private static String nowTimestamp() {
        return FORMATTER.format(Instant.now());
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
