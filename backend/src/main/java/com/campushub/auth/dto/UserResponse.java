package com.campushub.auth.dto;

import com.campushub.user.model.User;

import java.util.UUID;

/*
 * DTO chua thong tin User duoc phep
 * tra ve cho frontend.
 *
 * Khong tra truc tiep User entity ra API.
 *
 * Ly do:
 * User entity con chua cac thong tin noi bo nhu:
 *
 * - passwordHash
 * - role
 * - createdAt
 * - updatedAt
 *
 * Frontend Authentication hien tai chi can:
 *
 * - id
 * - fullName
 * - email
 */
public record UserResponse(

        /*
         * ID cua user.
         *
         * Backend dung UUID.
         *
         * Khi Jackson chuyen thanh JSON,
         * UUID se duoc gui ra dang chuoi:
         *
         * "550e8400-e29b-41d4-a716-446655440000"
         *
         * Phu hop voi frontend TypeScript:
         *
         * id: string
         */
        UUID id,

        // Ho va ten cua user.
        String fullName,

        // Email cua user.
        String email

) {

    /*
     * Chuyen User entity thanh UserResponse.
     *
     * Vi du:
     *
     * User entity
     *     ↓
     * UserResponse.from(user)
     *     ↓
     * Du lieu an toan gui ve frontend.
     *
     * Method nay co chu y KHONG copy:
     *
     * - passwordHash
     * - role
     * - createdAt
     * - updatedAt
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail()
        );
    }
}
