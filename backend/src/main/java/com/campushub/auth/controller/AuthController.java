package com.campushub.auth.controller;

import com.campushub.auth.dto.AuthResponse;
import com.campushub.auth.dto.LoginRequest;
import com.campushub.auth.dto.RegisterRequest;
import com.campushub.auth.dto.UserResponse;
import com.campushub.auth.security.AuthenticatedUser;
import com.campushub.auth.service.AuthService;
import com.campushub.user.model.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
 * Controller cung cap Authentication API
 * cho frontend CampusHub.
 *
 * Hien tai controller xu ly:
 *
 * - Register
 * - Login
 * - Lay user hien tai (/me)
 *
 * Logout KHONG con nam trong controller.
 *
 * POST /api/v1/auth/logout
 * duoc Spring Security LogoutFilter
 * xu ly trong SecurityConfig.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    /*
     * Business logic Authentication.
     */
    private final AuthService authService;

    /*
     * Dung de persistence SecurityContext
     * vao HttpSession.
     */
    private final SecurityContextRepository securityContextRepository;

    /*
     * Strategy duoc goi sau login thanh cong.
     *
     * Hien tai gom:
     *
     * - ChangeSessionIdAuthenticationStrategy
     * - CsrfAuthenticationStrategy
     *
     * Muc dich:
     *
     * - Chong session fixation.
     * - Xoa CSRF token cu sau login.
     */
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;

    /*
     * Constructor Injection.
     */
    public AuthController(
            AuthService authService,
            SecurityContextRepository securityContextRepository,
            SessionAuthenticationStrategy sessionAuthenticationStrategy
    ) {
        this.authService = authService;
        this.securityContextRepository = securityContextRepository;
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
    }

    /*
     * =========================================================
     * REGISTER
     * =========================================================
     *
     * POST /api/v1/auth/register
     *
     * Request:
     *
     * {
     *   "fullName": "Nguyen Van A",
     *   "email": "vana@example.com",
     *   "password": "12345678"
     * }
     *
     * Thanh cong:
     *
     * HTTP 201 Created
     *
     * Luu y:
     *
     * Vi CSRF da duoc bat,
     * request POST nay phai co:
     *
     * X-XSRF-TOKEN
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        AuthResponse response =
                authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /*
     * =========================================================
     * LOGIN
     * =========================================================
     *
     * POST /api/v1/auth/login
     *
     * Luong:
     *
     * LoginRequest
     *      ↓
     * AuthService.login()
     *      ↓
     * User
     *      ↓
     * AuthenticatedUser
     *      ↓
     * Authentication
     *      ↓
     * SessionAuthenticationStrategy
     *      ↓
     * session fixation protection
     *      +
     * CSRF token invalidation
     *      ↓
     * SecurityContext
     *      ↓
     * HttpSession
     *      ↓
     * JSESSIONID
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {

        /*
         * =====================================================
         * 1. KIEM TRA EMAIL + PASSWORD
         * =====================================================
         */
        User user =
                authService.login(request);

        /*
         * =====================================================
         * 2. TAO PRINCIPAL AN TOAN
         * =====================================================
         *
         * Khong luu User entity vao session.
         *
         * AuthenticatedUser chi chua:
         *
         * - id
         * - fullName
         * - email
         * - role
         */
        AuthenticatedUser principal =
                AuthenticatedUser.from(user);

        /*
         * =====================================================
         * 3. TAO AUTHORITY
         * =====================================================
         *
         * USER
         *      ↓
         * ROLE_USER
         *
         * ADMIN
         *      ↓
         * ROLE_ADMIN
         */
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority(
                        "ROLE_" + user.getRole().name()
                );

        /*
         * =====================================================
         * 4. TAO AUTHENTICATION
         * =====================================================
         *
         * credentials = null
         *
         * Khong luu password vao session.
         */
        Authentication authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        principal,
                        null,
                        List.of(authority)
                );

        /*
         * =====================================================
         * 5. SESSION AUTHENTICATION STRATEGY
         * =====================================================
         *
         * Spring Security se:
         *
         * - bao ve session fixation
         * - xoa CSRF token cu
         *
         * Sau login frontend phai lay
         * CSRF token moi.
         */
        sessionAuthenticationStrategy.onAuthentication(
                authentication,
                httpRequest,
                httpResponse
        );

        /*
         * =====================================================
         * 6. TAO SECURITY CONTEXT
         * =====================================================
         */
        SecurityContext securityContext =
                SecurityContextHolder.createEmptyContext();

        securityContext.setAuthentication(
                authentication
        );

        SecurityContextHolder.setContext(
                securityContext
        );

        /*
         * =====================================================
         * 7. LUU SECURITY CONTEXT VAO SESSION
         * =====================================================
         */
        securityContextRepository.saveContext(
                securityContext,
                httpRequest,
                httpResponse
        );

        /*
         * =====================================================
         * 8. RESPONSE
         * =====================================================
         *
         * Khong tra:
         *
         * - password
         * - passwordHash
         */
        return ResponseEntity.ok(
                AuthResponse.from(user)
        );
    }

    /*
     * =========================================================
     * CURRENT USER
     * =========================================================
     *
     * GET /api/v1/auth/me
     *
     * Endpoint nay bat buoc user da login.
     *
     * Luong:
     *
     * JSESSIONID
     *      ↓
     * HttpSession
     *      ↓
     * SecurityContext
     *      ↓
     * Authentication
     *      ↓
     * AuthenticatedUser
     */
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(
            Authentication authentication
    ) {

        AuthenticatedUser principal =
                (AuthenticatedUser) authentication.getPrincipal();

        UserResponse userResponse =
                new UserResponse(
                        principal.id(),
                        principal.fullName(),
                        principal.email()
                );

        return ResponseEntity.ok(
                new AuthResponse(userResponse)
        );
    }
}
