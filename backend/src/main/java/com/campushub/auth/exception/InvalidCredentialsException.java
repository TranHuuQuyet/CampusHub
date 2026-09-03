package com.campushub.auth.exception;

/*
 * Exception duoc su dung khi thong tin dang nhap
 * khong hop le.
 *
 * Co the xay ra khi:
 *
 * - Email khong ton tai.
 * - Password khong dung.
 *
 * Ca hai truong hop deu tra cung mot message:
 *
 * "Invalid email or password"
 *
 * Khong nen tra:
 *
 * "Email does not exist"
 *
 * hoac:
 *
 * "Password is incorrect"
 *
 * vi dieu do co the giup ke tan cong
 * doan duoc email nao dang ton tai trong he thong.
 *
 * Sau nay GlobalExceptionHandler se chuyen
 * exception nay thanh:
 *
 * HTTP 401 Unauthorized
 */
public class InvalidCredentialsException extends RuntimeException {

    /*
     * Constructor nhan message loi
     * va chuyen len RuntimeException.
     */
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
