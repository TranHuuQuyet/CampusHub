package com.campushub.auth.dto;

import java.util.Map;

/*
 * DTO chuan hoa response khi API xay ra loi.
 *
 * Frontend CampusHub hien tai co the nhan:
 *
 * {
 *   "message": "Validation failed",
 *   "fieldErrors": {
 *     "email": "Email format is invalid"
 *   }
 * }
 *
 * message:
 * Thong bao loi tong quat.
 *
 * fieldErrors:
 * Loi cua tung field neu co.
 *
 * Vi du:
 *
 * email -> "Email format is invalid"
 * password -> "Password must be between 8 and 72 characters"
 */
public record ApiError(

        // Thong bao loi tong quat.
        String message,

        /*
         * Loi validation cua tung field.
         *
         * Co the null neu loi khong lien quan
         * den mot field cu the.
         *
         * Vi du email da ton tai:
         *
         * {
         *   "message": "Email is already registered",
         *   "fieldErrors": null
         * }
         */
        Map<String, String> fieldErrors

) {

    /*
     * Helper cho cac loi khong co fieldErrors.
     *
     * Vi du:
     *
     * ApiError.of("Email is already registered")
     */
    public static ApiError of(String message) {
        return new ApiError(message, null);
    }
}
