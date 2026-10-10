package com.campushub.common.exception;

public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) 
    {
        super(message);
    }
    public AccessDeniedException(String message, Throwable cause)
    {
        super(message, cause); // super used to pass message error to parent class to throwable
    }
}
