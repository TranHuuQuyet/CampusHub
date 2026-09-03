package com.campushub.auth.exception;

import com.campushub.auth.dto.ApiError;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/*
 * Xu ly exception chung cho cac REST API cua CampusHub.
 *
 * @RestControllerAdvice cho phep class nay
 * bat exception tu cac Controller va chuyen
 * thanh JSON response phu hop.
 *
 * Luong tong quat:
 *
 * Controller
 *      ↓
 * Service
 *      ↓
 * throw Exception
 *      ↓
 * GlobalExceptionHandler
 *      ↓
 * HTTP status + ApiError JSON
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     * =========================================================
     * EMAIL DA TON TAI
     * =========================================================
     *
     * Xay ra khi user register bang email
     * da ton tai trong database.
     *
     * AuthService:
     *
     * existsByEmail(...) = true
     *      ↓
     * throw EmailAlreadyExistsException
     *
     * Response:
     *
     * HTTP 409 Conflict
     *
     * {
     *   "message": "Email is already registered",
     *   "fieldErrors": null
     * }
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleEmailAlreadyExists(
            EmailAlreadyExistsException exception
    ) {

        /*
         * Tao ApiError khong co fieldErrors.
         */
        ApiError error =
                ApiError.of(exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    /*
     * =========================================================
     * LOGIN SAI EMAIL HOAC PASSWORD
     * =========================================================
     *
     * Xay ra khi:
     *
     * - Email khong ton tai.
     * - Password khong dung.
     *
     * Ca hai truong hop deu tra cung mot message:
     *
     * "Invalid email or password"
     *
     * Khong nen phan biet:
     *
     * "Email does not exist"
     *
     * va:
     *
     * "Password is incorrect"
     *
     * vi co the lam lo email nao dang ton tai
     * trong he thong.
     *
     * Response:
     *
     * HTTP 401 Unauthorized
     *
     * {
     *   "message": "Invalid email or password",
     *   "fieldErrors": null
     * }
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(
            InvalidCredentialsException exception
    ) {

        /*
         * Tao ApiError voi message tu exception.
         */
        ApiError error =
                ApiError.of(exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(error);
    }

    /*
     * =========================================================
     * VALIDATION ERROR
     * =========================================================
     *
     * Xu ly loi validation tu @Valid.
     *
     * Vi du frontend gui:
     *
     * {
     *   "fullName": "",
     *   "email": "abc",
     *   "password": "123"
     * }
     *
     * RegisterRequest co:
     *
     * @NotBlank
     * @Email
     * @Size
     *
     * Khi validation that bai,
     * Spring se nem:
     *
     * MethodArgumentNotValidException
     *
     * Response:
     *
     * HTTP 400 Bad Request
     *
     * {
     *   "message": "Validation failed",
     *   "fieldErrors": {
     *     "fullName": "Full name is required",
     *     "email": "Email format is invalid",
     *     "password":
     *       "Password must be between 8 and 72 characters"
     *   }
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception
    ) {

        /*
         * LinkedHashMap giu thu tu insert cua cac loi,
         * giup response de doc hon khi debug.
         */
        Map<String, String> fieldErrors =
                new LinkedHashMap<>();

        /*
         * Lay tat ca FieldError do Bean Validation tao ra.
         *
         * Vi du:
         *
         * fullName
         * email
         * password
         */
        for (FieldError fieldError
                : exception.getBindingResult().getFieldErrors()) {

            /*
             * Mot field co the vi pham nhieu validation.
             *
             * Vi du password rong co the vi pham:
             *
             * @NotBlank
             * @Size
             *
             * putIfAbsent(...) giup chi lay message
             * dau tien cua moi field.
             *
             * Dieu nay tranh response bi trung loi
             * cho cung mot field.
             */
            fieldErrors.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        /*
         * Tao response loi validation.
         */
        ApiError error = new ApiError(
                "Validation failed",
                fieldErrors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }
}
