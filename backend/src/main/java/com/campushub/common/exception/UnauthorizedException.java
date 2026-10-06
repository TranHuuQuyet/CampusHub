package com.campushub.common.exception;

public class UnauthorizedException extends RuntimeException
{
    public UnauthorizedException(String message) 
    {
        super(message);
    }
    public UnauthorizedException(String message, Throwable cause)
    {
        super(message, cause); // super used to pass message error to parent class to throwable
    }
}
