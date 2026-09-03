package com.campushub.auth.dto;

import com.campushub.user.model.User;

/*
 * Response chung cho cac thao tac Authentication.
 *
 * JSON gui ve frontend:
 *
 * {
 *   "user": {
 *     "id": "...",
 *     "fullName": "...",
 *     "email": "..."
 *   }
 * }
 *
 * Duoc tai su dung cho:
 *
 * - Register
 * - Login
 * - GET /auth/me
 */
public record AuthResponse(

        // Thong tin user an toan de gui ra frontend.
        UserResponse user

) {

    /*
     * Chuyen User entity thanh AuthResponse.
     *
     * User
     *   ↓
     * UserResponse.from(user)
     *   ↓
     * AuthResponse
     */
    public static AuthResponse from(User user) {
        return new AuthResponse(
                UserResponse.from(user)
        );
    }
}
