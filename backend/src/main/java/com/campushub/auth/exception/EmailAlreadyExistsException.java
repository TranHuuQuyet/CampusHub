package com.campushub.auth.exception;

/*
 * Exception duoc nem ra khi nguoi dung
 * dang ky bang email da ton tai.
 *
 * Vi du:
 *
 * Database da co:
 *   student@campushub.com
 *
 * User tiep tuc register:
 *   student@campushub.com
 *
 * AuthService:
 *   ↓
 * existsByEmail(...) = true
 *   ↓
 * throw EmailAlreadyExistsException
 *
 * Sau nay GlobalExceptionHandler se chuyen
 * exception nay thanh HTTP:
 *
 * 409 Conflict
 */
public class EmailAlreadyExistsException extends RuntimeException {

    /*
     * Constructor nhan message mo ta loi.
     *
     * super(message) gui message len RuntimeException.
     */
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
