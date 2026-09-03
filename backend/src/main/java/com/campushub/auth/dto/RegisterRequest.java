package com.campushub.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/*
 * DTO dung de nhan du lieu dang ky tai khoan
 * tu frontend CampusHub.
 *
 * DTO = Data Transfer Object.
 *
 * Khong nen dung truc tiep User entity
 * de nhan request tu frontend.
 *
 * Ly do:
 * Client chi duoc phep gui nhung truong
 * ma backend cho phep.
 *
 * Register request hien tai gom:
 *
 * - fullName
 * - email
 * - password
 *
 * Client KHONG duoc phep quyet dinh:
 *
 * - id
 * - role
 * - passwordHash
 * - createdAt
 * - updatedAt
 */
public record RegisterRequest(

        /*
         * Ho va ten cua nguoi dung.
         *
         * @NotBlank:
         * Khong chap nhan:
         *
         * null
         * ""
         * "   "
         *
         * @Size(max = 100):
         * Gioi han do dai de phu hop voi
         * User.fullName trong database.
         */
        @NotBlank(message = "Full name is required")
        @Size(
                max = 100,
                message = "Full name must not exceed 100 characters"
        )
        String fullName,

        /*
         * Email dung de dang ky va dang nhap.
         *
         * @NotBlank:
         * Email bat buoc phai co.
         *
         * @Email:
         * Kiem tra format email co hop le hay khong.
         *
         * @Size(max = 254):
         * Gioi han do dai email.
         *
         * Viec kiem tra email da ton tai
         * trong database hay chua se do
         * AuthService xu ly sau.
         */
        @NotBlank(message = "Email is required")
        @Email(message = "Email format is invalid")
        @Size(
                max = 254,
                message = "Email must not exceed 254 characters"
        )
        String email,

        /*
         * Password goc nguoi dung gui len.
         *
         * Password nay CHI ton tai trong request.
         *
         * Sau nay AuthService se:
         *
         * password
         *     ↓
         * PasswordEncoder
         *     ↓
         * BCrypt hash
         *     ↓
         * passwordHash
         *     ↓
         * User
         *     ↓
         * MySQL
         *
         * Tuyet doi khong luu password goc
         * vao database.
         */
        @NotBlank(message = "Password is required")
        @Size(
                min = 8,
                max = 72,
                message = "Password must be between 8 and 72 characters"
        )
        String password

) {
}
