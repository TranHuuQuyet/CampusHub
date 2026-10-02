package com.campushub.common.dto;
import java.time.LocalDateTime;
import java.util.Map;


public class ApiErrorResponse {
    private int status;
    private String error;
    private String message;
    private Map <String,String> fields;
    private LocalDateTime timestamp = LocalDateTime.now();

    public ApiErrorResponse(int status, String error, String message, Map<String,String> fields) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.fields = fields;
    }
    public static ApiErrorResponse of(int status, String error, String message) 
    {
        return new ApiErrorResponse(status, error, message, null);
    }

    public static ApiErrorResponse of(int status, String error, String message, Map<String,String> fields) {
        return new ApiErrorResponse(status, error, message, fields);
    }

    public int getStatus() {
        return status;
    }
    public String getError() {
        return error;
    }
    public String getMessage() {
        return message; 
    }
    public Map<String,String> getFields() {
        return fields;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

}

