package com.liferay.common.http.request.api.constants;

import java.util.Map;

public class DataNotFoundException extends ApiException {

    public DataNotFoundException(String message) {
        super(message, DATA_NOT_FOUND);  // Status code 404 for data not found
    }

    public DataNotFoundException(String message, Map<String, String> fieldErrors) {
        super(message, DATA_NOT_FOUND, fieldErrors);
    }

    public DataNotFoundException(String field, String errorMessage) {
        super("Data not found for the provided field(s)", DATA_NOT_FOUND);
        addFieldError(field, errorMessage);  // Add a specific field error
    }
}