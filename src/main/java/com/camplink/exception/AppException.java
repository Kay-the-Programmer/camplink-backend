package com.camplink.exception;

import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {
    private final HttpStatus status;

    public AppException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }

    public static AppException notFound(String msg) {
        return new AppException(HttpStatus.NOT_FOUND, msg);
    }

    public static AppException forbidden(String msg) {
        return new AppException(HttpStatus.FORBIDDEN, msg);
    }

    public static AppException badRequest(String msg) {
        return new AppException(HttpStatus.BAD_REQUEST, msg);
    }

    public static AppException conflict(String msg) {
        return new AppException(HttpStatus.CONFLICT, msg);
    }
}
