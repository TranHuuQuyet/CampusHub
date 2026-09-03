package com.campushub.auth.security;

import com.campushub.user.model.Role;
import com.campushub.user.model.User;

import java.io.Serializable;
import java.util.UUID;

/*
 * Dai dien cho user da duoc Authentication thanh cong.
 *
 * Object nay se duoc dat vao Spring SecurityContext
 * va co the duoc luu trong HttpSession.
 *
 * KHONG luu nguyen User entity vao session.
 *
 * Ly do:
 *
 * User entity con chua:
 *
 * - passwordHash
 * - createdAt
 * - updatedAt
 *
 * Session chi can cac thong tin phuc vu:
 *
 * - xac dinh user hien tai
 * - hien thi thong tin co ban
 * - authorization theo role
 */
public record AuthenticatedUser(

        // ID cua user trong database.
        UUID id,

        // Ho ten user.
        String fullName,

        // Email dang nhap.
        String email,

        /*
         * Role dung cho authorization.
         *
         * Vi du:
         *
         * USER
         * ADMIN
         */
        Role role

) implements Serializable {

    /*
     * Chuyen User entity thanh principal
     * an toan de luu trong SecurityContext.
     *
     * Co chu y KHONG copy:
     *
     * - passwordHash
     * - createdAt
     * - updatedAt
     */
    public static AuthenticatedUser from(User user) {
        return new AuthenticatedUser(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
