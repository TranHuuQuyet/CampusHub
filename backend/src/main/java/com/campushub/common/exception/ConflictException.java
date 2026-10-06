//Use for 409
package com.campushub.common.exception;

public class ConflictException extends RuntimeException {
    public ConflictException (String message){
        super(message);
    }
    public ConflictException (String message, Throwable cause) {
        super(message, cause); // super used to pass message error to parent class to throwable 
    }
}
