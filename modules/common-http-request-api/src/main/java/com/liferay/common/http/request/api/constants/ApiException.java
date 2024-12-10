package com.liferay.common.http.request.api.constants;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ApiException extends RuntimeException implements Serializable {

    // Predefined HTTP Status Codes
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int DATA_NOT_FOUND = 404;
    public static final int INTERNAL_SERVER_ERROR = 500;

    private static final long serialVersionUID = 1L; // For serialization

    private int statusCode;
    private Map<String, String> fieldErrors;
    private Throwable throwable; // New field for the throwable

    public ApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
        this.fieldErrors = new HashMap<>();
    }

    public ApiException(String message, int statusCode, Map<String, String> fieldErrors) {
        super(message);
        this.statusCode = statusCode;
        this.fieldErrors = new HashMap<>(fieldErrors); // Make a defensive copy
    }

    public ApiException(String message, int statusCode, Throwable throwable) {
        super(message, throwable);
        this.statusCode = statusCode;
        this.fieldErrors = new HashMap<>();
        this.throwable = throwable;
    }

    public ApiException(String message, int statusCode, Map<String, String> fieldErrors, Throwable throwable) {
        super(message, throwable);
        this.statusCode = statusCode;
        this.fieldErrors = new HashMap<>(fieldErrors); // Make a defensive copy
        this.throwable = throwable;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, String> getFieldErrors() {
        return Collections.unmodifiableMap(fieldErrors); // Return unmodifiable map
    }

    public void addFieldError(String field, String errorMessage) {
        this.fieldErrors.put(field, errorMessage);
    }

    public String getThrowableMessage() {
        return throwable != null ? throwable.getMessage() : null;
    }

    @Override
    public String toString() {
        return String.format("ApiException{message='%s', statusCode=%d, fieldErrors=%s, throwableMessage=%s}",
                getMessage(), statusCode, fieldErrors, getThrowableMessage());
    }
}