package com.campushub.auth;

import com.campushub.user.model.Role;
import com.campushub.user.model.User;
import com.campushub.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Integration tests cho Authentication API.
 *
 * Test load full Spring Boot application:
 *
 * - Controller
 * - Service
 * - Repository
 * - Spring Security
 * - Validation
 * - HttpSession
 * - CSRF
 * - Authorization
 * - USER / ADMIN
 * - Logout
 * - H2 database
 *
 * Khong dung MySQL that.
 *
 * =========================================================
 * CAC TEST HIEN TAI
 * =========================================================
 *
 * REGISTER:
 *
 * - Register hop le        -> 201
 * - Email trung            -> 409
 * - Validation sai         -> 400
 *
 * LOGIN:
 *
 * - Credentials dung       -> 200
 * - Password sai           -> 401
 * - Email khong ton tai    -> 401
 *
 * SESSION / ME:
 *
 * - Login -> /auth/me      -> 200
 * - Khong login -> /me     -> 401
 *
 * CSRF:
 *
 * - POST login co CSRF     -> 200
 * - POST login khong CSRF  -> 403
 *
 * AUTHORIZATION:
 *
 * - ROLE_USER  -> /admin/test -> 403
 * - ROLE_ADMIN -> /admin/test -> 200
 *
 * LOGOUT:
 *
 * - Login
 * - /auth/me -> 200
 * - Logout -> 204
 * - Session bi invalidate
 * - /auth/me khong session -> 401
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTests {

    /*
     * MockMvc cho phep test HTTP request
     * ma khong can start Tomcat that.
     */
    @Autowired
    private MockMvc mockMvc;

    /*
     * Repository dung de:
     *
     * - Tao test data.
     * - Xoa test data.
     *
     * Database test hien tai la H2.
     */
    @Autowired
    private UserRepository userRepository;

    /*
     * PasswordEncoder that cua application.
     *
     * CampusHub hien tai dung BCrypt.
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /*
     * =========================================================
     * BEFORE EACH TEST
     * =========================================================
     *
     * Xoa toan bo user trong H2
     * truoc moi test.
     */
    @BeforeEach
    void setUp() {

        userRepository.deleteAll();
    }

    /*
     * =========================================================
     * REGISTER - SUCCESS
     * =========================================================
     */
    @Test
    void registerWithValidRequestReturnsCreated() throws Exception {

        String requestBody = """
                {
                  "fullName": "Nguyen Van A",
                  "email": "vana@example.com",
                  "password": "12345678"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        jsonPath("$.user.id").exists()
                )
                .andExpect(
                        jsonPath("$.user.fullName")
                                .value("Nguyen Van A")
                )
                .andExpect(
                        jsonPath("$.user.email")
                                .value("vana@example.com")
                );
    }

    /*
     * =========================================================
     * REGISTER - DUPLICATE EMAIL
     * =========================================================
     */
    @Test
    void registerWithDuplicateEmailReturnsConflict() throws Exception {

        String requestBody = """
                {
                  "fullName": "Nguyen Van A",
                  "email": "duplicate@example.com",
                  "password": "12345678"
                }
                """;

        /*
         * Register lan 1.
         */
        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(
                        status().isCreated()
                );

        /*
         * Register lan 2 voi cung email.
         */
        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(
                        status().isConflict()
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Email is already registered")
                );
    }

    /*
     * =========================================================
     * REGISTER - VALIDATION ERROR
     * =========================================================
     */
    @Test
    void registerWithInvalidRequestReturnsBadRequest() throws Exception {

        String requestBody = """
                {
                  "fullName": "",
                  "email": "abc",
                  "password": "123"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Validation failed")
                )
                .andExpect(
                        jsonPath("$.fieldErrors.fullName").exists()
                )
                .andExpect(
                        jsonPath("$.fieldErrors.email").exists()
                )
                .andExpect(
                        jsonPath("$.fieldErrors.password").exists()
                );
    }

    /*
     * =========================================================
     * LOGIN - SUCCESS
     * =========================================================
     */
    @Test
    void loginWithValidCredentialsReturnsOk() throws Exception {

        User user = new User(
                "Nguyen Van A",
                "vana@example.com",
                passwordEncoder.encode("12345678"),
                Role.USER
        );

        userRepository.save(user);

        String requestBody = """
                {
                  "email": "vana@example.com",
                  "password": "12345678"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.user.id").exists()
                )
                .andExpect(
                        jsonPath("$.user.fullName")
                                .value("Nguyen Van A")
                )
                .andExpect(
                        jsonPath("$.user.email")
                                .value("vana@example.com")
                );
    }

    /*
     * =========================================================
     * LOGIN - WRONG PASSWORD
     * =========================================================
     */
    @Test
    void loginWithWrongPasswordReturnsUnauthorized() throws Exception {

        User user = new User(
                "Nguyen Van A",
                "vana@example.com",
                passwordEncoder.encode("12345678"),
                Role.USER
        );

        userRepository.save(user);

        String requestBody = """
                {
                  "email": "vana@example.com",
                  "password": "wrong-password"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(
                        status().isUnauthorized()
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Invalid email or password")
                );
    }

    /*
     * =========================================================
     * LOGIN - EMAIL NOT FOUND
     * =========================================================
     *
     * Message phai giong password sai
     * de tranh user enumeration.
     */
    @Test
    void loginWithUnknownEmailReturnsUnauthorized() throws Exception {

        String requestBody = """
                {
                  "email": "notfound@example.com",
                  "password": "12345678"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(
                        status().isUnauthorized()
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Invalid email or password")
                );
    }

    /*
     * =========================================================
     * SESSION / ME - SUCCESS
     * =========================================================
     *
     * Login
     *      ↓
     * SecurityContext
     *      ↓
     * HttpSession
     *      ↓
     * GET /auth/me
     *      ↓
     * HTTP 200
     */
    @Test
    void meAfterLoginReturnsCurrentUser() throws Exception {

        User user = new User(
                "Nguyen Van A",
                "vana@example.com",
                passwordEncoder.encode("12345678"),
                Role.USER
        );

        userRepository.save(user);

        String loginRequest = """
                {
                  "email": "vana@example.com",
                  "password": "12345678"
                }
                """;

        MvcResult loginResult =
                mockMvc.perform(
                                post("/api/v1/auth/login")
                                        .with(csrf())
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(loginRequest)
                        )
                        .andExpect(
                                status().isOk()
                        )
                        .andReturn();

        MockHttpSession session =
                (MockHttpSession) loginResult
                        .getRequest()
                        .getSession(false);

        assertNotNull(session);

        mockMvc.perform(
                        get("/api/v1/auth/me")
                                .session(session)
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.user.id").exists()
                )
                .andExpect(
                        jsonPath("$.user.fullName")
                                .value("Nguyen Van A")
                )
                .andExpect(
                        jsonPath("$.user.email")
                                .value("vana@example.com")
                );
    }

    /*
     * =========================================================
     * SESSION / ME - NOT AUTHENTICATED
     * =========================================================
     */
    @Test
    void meWithoutLoginReturnsUnauthorized() throws Exception {

        mockMvc.perform(
                        get("/api/v1/auth/me")
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    /*
     * =========================================================
     * CSRF - MISSING TOKEN
     * =========================================================
     *
     * Login la POST request.
     *
     * Neu khong co CSRF token:
     *
     * HTTP 403 Forbidden.
     */
    @Test
    void loginWithoutCsrfReturnsForbidden() throws Exception {

        User user = new User(
                "Nguyen Van A",
                "vana@example.com",
                passwordEncoder.encode("12345678"),
                Role.USER
        );

        userRepository.save(user);

        String requestBody = """
                {
                  "email": "vana@example.com",
                  "password": "12345678"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                /*
                                 * Co tinh KHONG them:
                                 *
                                 * .with(csrf())
                                 */
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(
                        status().isForbidden()
                );
    }

    /*
     * =========================================================
     * AUTHORIZATION - USER CANNOT ACCESS ADMIN API
     * =========================================================
     *
     * ROLE_USER
     *      ↓
     * Login
     *      ↓
     * GET /api/v1/admin/test
     *      ↓
     * 403 Forbidden
     */
    @Test
    void userRoleCannotAccessAdminEndpoint() throws Exception {

        User user = new User(
                "Normal User",
                "user@example.com",
                passwordEncoder.encode("12345678"),
                Role.USER
        );

        userRepository.save(user);

        String loginRequest = """
                {
                  "email": "user@example.com",
                  "password": "12345678"
                }
                """;

        MvcResult loginResult =
                mockMvc.perform(
                                post("/api/v1/auth/login")
                                        .with(csrf())
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(loginRequest)
                        )
                        .andExpect(
                                status().isOk()
                        )
                        .andReturn();

        MockHttpSession session =
                (MockHttpSession) loginResult
                        .getRequest()
                        .getSession(false);

        assertNotNull(session);

        mockMvc.perform(
                        get("/api/v1/admin/test")
                                .session(session)
                )
                .andExpect(
                        status().isForbidden()
                );
    }

    /*
     * =========================================================
     * AUTHORIZATION - ADMIN CAN ACCESS ADMIN API
     * =========================================================
     *
     * ROLE_ADMIN
     *      ↓
     * Login
     *      ↓
     * GET /api/v1/admin/test
     *      ↓
     * 200 OK
     */
    @Test
    void adminRoleCanAccessAdminEndpoint() throws Exception {

        User admin = new User(
                "CampusHub Admin",
                "admin@example.com",
                passwordEncoder.encode("Admin12345!"),
                Role.ADMIN
        );

        userRepository.save(admin);

        String loginRequest = """
                {
                  "email": "admin@example.com",
                  "password": "Admin12345!"
                }
                """;

        MvcResult loginResult =
                mockMvc.perform(
                                post("/api/v1/auth/login")
                                        .with(csrf())
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(loginRequest)
                        )
                        .andExpect(
                                status().isOk()
                        )
                        .andReturn();

        MockHttpSession session =
                (MockHttpSession) loginResult
                        .getRequest()
                        .getSession(false);

        assertNotNull(session);

        mockMvc.perform(
                        get("/api/v1/admin/test")
                                .session(session)
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Admin access granted")
                );
    }

    /*
     * =========================================================
     * LOGOUT - INVALIDATE AUTHENTICATED SESSION
     * =========================================================
     *
     * Muc tieu:
     *
     * Chung minh Spring Security Logout
     * thuc su invalidate HttpSession.
     *
     * Luong:
     *
     * Tao USER
     *      ↓
     * Login
     *      ↓
     * Session duoc tao
     *      ↓
     * GET /auth/me -> 200
     *      ↓
     * POST /auth/logout
     *      ↓
     * 204 No Content
     *      ↓
     * HttpSession bi invalidate
     *      ↓
     * GET /auth/me khong authentication
     *      ↓
     * 401 Unauthorized
     */
    @Test
    void logoutInvalidatesAuthenticatedSession() throws Exception {

        /*
         * =====================================================
         * 1. TAO USER
         * =====================================================
         */
        User user = new User(
                "Nguyen Van A",
                "vana@example.com",
                passwordEncoder.encode("12345678"),
                Role.USER
        );

        userRepository.save(user);

        String loginRequest = """
                {
                  "email": "vana@example.com",
                  "password": "12345678"
                }
                """;

        /*
         * =====================================================
         * 2. LOGIN
         * =====================================================
         */
        MvcResult loginResult =
                mockMvc.perform(
                                post("/api/v1/auth/login")
                                        .with(csrf())
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(loginRequest)
                        )
                        .andExpect(
                                status().isOk()
                        )
                        .andReturn();

        /*
         * =====================================================
         * 3. LAY SESSION
         * =====================================================
         */
        MockHttpSession session =
                (MockHttpSession) loginResult
                        .getRequest()
                        .getSession(false);

        assertNotNull(session);

        /*
         * =====================================================
         * 4. KIEM TRA TRUOC LOGOUT
         * =====================================================
         *
         * Session dang authenticated.
         */
        mockMvc.perform(
                        get("/api/v1/auth/me")
                                .session(session)
                )
                .andExpect(
                        status().isOk()
                );

        /*
         * =====================================================
         * 5. LOGOUT
         * =====================================================
         *
         * Logout la POST request.
         *
         * Vi CampusHub bat CSRF,
         * request logout phai co CSRF token hop le.
         */
        mockMvc.perform(
                        post("/api/v1/auth/logout")
                                .session(session)
                                .with(csrf())
                )
                .andExpect(
                        status().isNoContent()
                );

        /*
         * =====================================================
         * 6. SESSION PHAI BI INVALIDATE
         * =====================================================
         *
         * Spring Security LogoutFilter mac dinh
         * su dung SecurityContextLogoutHandler.
         *
         * Handler nay invalidate HttpSession.
         */
        assertTrue(session.isInvalid());

        /*
         * =====================================================
         * 7. SAU LOGOUT KHONG CON AUTHENTICATION
         * =====================================================
         *
         * Khong reuse session da invalidate.
         *
         * Trong browser that:
         *
         * JSESSIONID cu da bi xoa / khong con hop le.
         *
         * Request moi den /auth/me
         * phai bi 401.
         */
        mockMvc.perform(
                        get("/api/v1/auth/me")
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }
}
