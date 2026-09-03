package com.campushub.auth.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * Controller cung cap CSRF token
 * cho React frontend.
 *
 * React se goi endpoint nay
 * truoc cac request thay doi du lieu.
 *
 * Vi du:
 *
 * POST /auth/login
 * POST /auth/register
 * POST /auth/logout
 * PUT
 * PATCH
 * DELETE
 */
@RestController
@RequestMapping("/api/v1")
public class CsrfController {

    /*
     * GET /api/v1/csrf
     *
     * Khi Spring inject CsrfToken vao method,
     * token se duoc load/generate.
     *
     * Sau khi cau hinh CSRF SPA,
     * backend se gui token cho browser
     * thong qua XSRF-TOKEN cookie.
     */
    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken csrfToken) {
        return csrfToken;
    }
}
