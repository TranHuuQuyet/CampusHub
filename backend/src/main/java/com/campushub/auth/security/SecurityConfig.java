package com.campushub.auth.security;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfTokenRepository;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/*
 * Cau hinh Spring Security cho Backend CampusHub.
 *
 * Hien tai:
 *
 * PUBLIC:
 *
 * - GET  /api/v1/health
 * - GET  /api/v1/csrf
 * - POST /api/v1/auth/register
 * - POST /api/v1/auth/login
 *
 * AUTHENTICATED:
 *
 * - GET /api/v1/auth/me
 * - Cac API thong thuong khac
 *
 * ADMIN:
 *
 * - /api/v1/admin/**
 *
 * LOGOUT:
 *
 * - POST /api/v1/auth/logout
 * - Duoc Spring Security LogoutFilter xu ly.
 *
 * Security:
 *
 * - Authentication luu trong HttpSession.
 * - Session fixation protection.
 * - JSESSIONID HttpOnly.
 * - CORS cho React localhost:5173.
 * - CSRF cho React SPA.
 * - CSRF token rotate sau login.
 * - CSRF token clear sau logout.
 * - USER / ADMIN authorization.
 */
@Configuration
public class SecurityConfig {

    /*
     * =========================================================
     * SECURITY CONTEXT REPOSITORY
     * =========================================================
     *
     * Luu SecurityContext vao HttpSession.
     *
     * Login
     *      ↓
     * Authentication
     *      ↓
     * SecurityContext
     *      ↓
     * HttpSession
     *      ↓
     * JSESSIONID
     */
    @Bean
    SecurityContextRepository securityContextRepository() {

        return new HttpSessionSecurityContextRepository();
    }

    /*
     * =========================================================
     * CSRF TOKEN REPOSITORY
     * =========================================================
     *
     * CSRF token duoc luu trong:
     *
     * XSRF-TOKEN cookie
     *
     * React doc cookie nay va gui lai qua:
     *
     * X-XSRF-TOKEN header
     *
     * withHttpOnlyFalse():
     *
     * Cho phep JavaScript doc XSRF-TOKEN.
     *
     * JSESSIONID van duoc giu HttpOnly.
     */
    @Bean
    CsrfTokenRepository csrfTokenRepository() {

        CookieCsrfTokenRepository repository =
                CookieCsrfTokenRepository.withHttpOnlyFalse();

        /*
         * Cookie CSRF co the duoc gui
         * cho toan bo backend.
         */
        repository.setCookiePath("/");

        return repository;
    }

    /*
     * =========================================================
     * SESSION AUTHENTICATION STRATEGY
     * =========================================================
     *
     * Duoc AuthController goi
     * sau khi login thanh cong.
     *
     * Gom:
     *
     * 1. ChangeSessionIdAuthenticationStrategy
     *
     *    Bao ve session fixation.
     *
     * 2. CsrfAuthenticationStrategy
     *
     *    Xoa CSRF token cu sau login.
     *
     * Luong:
     *
     * Login thanh cong
     *      ↓
     * doi session ID
     *      ↓
     * xoa XSRF-TOKEN cu
     *      ↓
     * GET /csrf
     *      ↓
     * tao XSRF-TOKEN moi
     */
    @Bean
    SessionAuthenticationStrategy sessionAuthenticationStrategy(
            CsrfTokenRepository csrfTokenRepository
    ) {

        ChangeSessionIdAuthenticationStrategy sessionFixationStrategy =
                new ChangeSessionIdAuthenticationStrategy();

        CsrfAuthenticationStrategy csrfAuthenticationStrategy =
                new CsrfAuthenticationStrategy(
                        csrfTokenRepository
                );

        return new CompositeSessionAuthenticationStrategy(
                List.of(
                        sessionFixationStrategy,
                        csrfAuthenticationStrategy
                )
        );
    }

    /*
     * =========================================================
     * CORS
     * =========================================================
     *
     * Frontend:
     *
     * http://localhost:5173
     *
     * Backend:
     *
     * http://localhost:8080
     *
     * CampusHub dung:
     *
     * credentials: "include"
     *
     * de gui JSESSIONID cookie.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        /*
         * Chi cho phep React frontend local.
         *
         * Khong dung "*"
         * khi allowCredentials = true.
         */
        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:5173"
                )
        );

        /*
         * HTTP methods frontend
         * co the su dung.
         */
        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        /*
         * Headers frontend duoc phep gui.
         *
         * Content-Type:
         * dung cho JSON.
         *
         * X-XSRF-TOKEN:
         * dung cho CSRF.
         */
        configuration.setAllowedHeaders(
                List.of(
                        "Content-Type",
                        "X-XSRF-TOKEN"
                )
        );

        /*
         * Cho phep browser gui cookie:
         *
         * - JSESSIONID
         * - XSRF-TOKEN
         */
        configuration.setAllowCredentials(true);

        /*
         * Cache preflight 1 gio.
         */
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/api/**",
                configuration
        );

        return source;
    }

    /*
     * =========================================================
     * SECURITY FILTER CHAIN
     * =========================================================
     */
    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository,
            CorsConfigurationSource corsConfigurationSource,
            CsrfTokenRepository csrfTokenRepository
    ) throws Exception {

        http

                /*
                 * =================================================
                 * CORS
                 * =================================================
                 */
                .cors(cors -> cors
                        .configurationSource(
                                corsConfigurationSource
                        )
                )

                /*
                 * =================================================
                 * AUTHORIZATION
                 * =================================================
                 *
                 * QUAN TRONG:
                 *
                 * Spring Security doc cac rule
                 * tu tren xuong duoi.
                 *
                 * Vi vay:
                 *
                 * public
                 *      ↓
                 * admin
                 *      ↓
                 * anyRequest
                 *
                 * anyRequest() phai nam cuoi.
                 */
                .authorizeHttpRequests(authorize -> authorize

                        /*
                         * =================================================
                         * PUBLIC - HEALTH
                         * =================================================
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/health"
                        ).permitAll()

                        /*
                         * =================================================
                         * PUBLIC - CSRF
                         * =================================================
                         *
                         * React can lay CSRF token
                         * truoc khi dang nhap.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/csrf"
                        ).permitAll()

                        /*
                         * =================================================
                         * PUBLIC - REGISTER / LOGIN
                         * =================================================
                         *
                         * Public ve Authentication.
                         *
                         * User chua login van co the:
                         *
                         * - register
                         * - login
                         *
                         * Nhung vi day la POST,
                         * request van phai co
                         * CSRF token hop le.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/register",
                                "/api/v1/auth/login"
                        ).permitAll()

                        /*
                         * =================================================
                         * ADMIN AUTHORIZATION
                         * =================================================
                         *
                         * Tat ca endpoint:
                         *
                         * /api/v1/admin/**
                         *
                         * chi cho phep:
                         *
                         * ROLE_ADMIN
                         *
                         * hasRole("ADMIN")
                         *
                         * Spring Security tu them prefix:
                         *
                         * ROLE_
                         *
                         * Nen:
                         *
                         * hasRole("ADMIN")
                         *
                         * se kiem tra:
                         *
                         * ROLE_ADMIN
                         *
                         * KHONG viet:
                         *
                         * hasRole("ROLE_ADMIN")
                         *
                         * Ket qua:
                         *
                         * Chua login:
                         *      -> 401
                         *
                         * ROLE_USER:
                         *      -> 403
                         *
                         * ROLE_ADMIN:
                         *      -> cho phep request
                         */
                        .requestMatchers(
                                "/api/v1/admin/**"
                        ).hasRole("ADMIN")

                        /*
                         * =================================================
                         * AUTHENTICATED
                         * =================================================
                         *
                         * Cac endpoint con lai
                         * chi yeu cau user da dang nhap.
                         *
                         * Vi du:
                         *
                         * GET /api/v1/auth/me
                         */
                        .anyRequest().authenticated()
                )

                /*
                 * =================================================
                 * SECURITY CONTEXT
                 * =================================================
                 *
                 * Luu Authentication vao HttpSession.
                 */
                .securityContext(securityContext -> securityContext
                        .securityContextRepository(
                                securityContextRepository
                        )
                )

                /*
                 * =================================================
                 * SESSION
                 * =================================================
                 *
                 * IF_REQUIRED:
                 *
                 * Khong tao session
                 * cho moi request.
                 *
                 * Chi tao khi can.
                 */
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED
                        )
                )

                /*
                 * =================================================
                 * REQUEST CACHE
                 * =================================================
                 *
                 * CampusHub la REST API.
                 *
                 * Khong can SavedRequest
                 * va redirect flow.
                 */
                .requestCache(cache -> cache.disable())

                /*
                 * =================================================
                 * FORM LOGIN
                 * =================================================
                 *
                 * React co LoginForm rieng.
                 */
                .formLogin(form -> form.disable())

                /*
                 * =================================================
                 * HTTP BASIC
                 * =================================================
                 *
                 * CampusHub khong dung:
                 *
                 * Authorization: Basic ...
                 */
                .httpBasic(basic -> basic.disable())

                /*
                 * =================================================
                 * CSRF
                 * =================================================
                 *
                 * Cau hinh CSRF danh cho SPA.
                 *
                 * Bao ve:
                 *
                 * POST
                 * PUT
                 * PATCH
                 * DELETE
                 *
                 * Spring Security va
                 * CsrfAuthenticationStrategy
                 * dung cung CsrfTokenRepository.
                 */
                .csrf(csrf -> csrf
                        .spa()
                        .csrfTokenRepository(
                                csrfTokenRepository
                        )
                )

                /*
                 * =================================================
                 * LOGOUT
                 * =================================================
                 *
                 * Spring Security LogoutFilter
                 * xu ly:
                 *
                 * POST /api/v1/auth/logout
                 *
                 * Request logout van phai
                 * co CSRF token hop le.
                 *
                 * Logout thanh cong se:
                 *
                 * - invalidate HttpSession
                 * - clear Authentication
                 * - clear SecurityContext
                 * - clear SecurityContextRepository
                 * - clear XSRF-TOKEN
                 * - delete JSESSIONID
                 * - tra HTTP 204
                 */
                .logout(logout -> logout

                        /*
                         * Endpoint logout cua CampusHub.
                         */
                        .logoutUrl(
                                "/api/v1/auth/logout"
                        )

                        /*
                         * Xoa session cookie.
                         */
                        .deleteCookies(
                                "JSESSIONID"
                        )

                        /*
                         * REST API:
                         *
                         * khong redirect,
                         * chi tra 204.
                         */
                        .logoutSuccessHandler(
                                (
                                        request,
                                        response,
                                        authentication
                                ) ->
                                        response.setStatus(
                                                HttpServletResponse.SC_NO_CONTENT
                                        )
                        )
                )

                /*
                 * =================================================
                 * ERROR RESPONSE
                 * =================================================
                 */
                .exceptionHandling(exception -> exception

                        /*
                         * Chua login ma truy cap
                         * endpoint duoc bao ve:
                         *
                         * 401 Unauthorized
                         */
                        .authenticationEntryPoint(
                                (
                                        request,
                                        response,
                                        authException
                                ) ->
                                        response.setStatus(
                                                HttpServletResponse.SC_UNAUTHORIZED
                                        )
                        )

                        /*
                         * User da login
                         * nhung khong co quyen:
                         *
                         * 403 Forbidden
                         *
                         * Vi du:
                         *
                         * ROLE_USER
                         * truy cap /api/v1/admin/**
                         */
                        .accessDeniedHandler(
                                (
                                        request,
                                        response,
                                        accessDeniedException
                                ) ->
                                        response.setStatus(
                                                HttpServletResponse.SC_FORBIDDEN
                                        )
                        )
                );

        return http.build();
    }
}
