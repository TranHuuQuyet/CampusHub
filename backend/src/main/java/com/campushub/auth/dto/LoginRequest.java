package com.campushub.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/*
 * DTO nhan du lieu dang nhap tu frontend.
 *
 * Frontend gui:
 *
 * {
 *   "email": "vana@example.com",
 *   "password": "12345678"
 * }
 */
public record LoginRequest(

        /*
         * Email bat buoc phai co
         * va dung dinh dang email.
         */
        @NotBlank(message = "Email is required")
        @Email(message = "Email format is invalid")
        @Size(
                max = 254,
                message = "Email must not exceed 254 characters"
        )
        String email,

        /*
         * Password bat buoc phai co.
         *
         * Gioi han 72 ky tu de phu hop
         * voi quy uoc password cua register.
         */
        @NotBlank(message = "Password is required")
        @Size(
                max = 72,
                message = "Password must not exceed 72 characters"
        )
        String password

) {
}
