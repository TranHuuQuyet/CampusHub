//Use for 404 
package com.campushub.common.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException (String message) {
        super(message);
    }
    public ResourceNotFoundException (String message, Throwable cause) {
        super(message, cause); // super used to pass message error to parent class to throwable 
    }
}
