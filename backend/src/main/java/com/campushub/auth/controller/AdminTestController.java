package com.campushub.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/*
 * Controller tam thoi dung de kiem tra
 * authorization ADMIN cua CampusHub.
 *
 * Endpoint:
 *
 * GET /api/v1/admin/test
 *
 * Muc tieu:
 *
 * - Chua login -> 401
 * - ROLE_USER  -> 403
 * - ROLE_ADMIN -> 200
 *
 * Sau khi cac module Admin that duoc tao,
 * endpoint test nay co the duoc xoa.
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminTestController {

    /*
     * Endpoint kiem tra quyen ADMIN.
     *
     * Viec chan USER khong thuc hien
     * trong controller.
     *
     * SecurityConfig se dam nhiem
     * authorization.
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Admin access granted"
                )
        );
    }
}
