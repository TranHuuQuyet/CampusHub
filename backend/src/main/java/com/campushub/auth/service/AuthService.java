package com.campushub.auth.service;

import com.campushub.auth.dto.AuthResponse;
import com.campushub.auth.dto.LoginRequest;
import com.campushub.auth.dto.RegisterRequest;
import com.campushub.auth.exception.EmailAlreadyExistsException;
import com.campushub.auth.exception.InvalidCredentialsException;
import com.campushub.user.model.Role;
import com.campushub.user.model.User;
import com.campushub.user.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/*
 * Service xu ly business logic lien quan den Authentication.
 *
 * Controller chi nen xu ly lop HTTP:
 *
 * HTTP request
 *      ↓
 * Controller
 *      ↓
 * AuthService
 *      ↓
 * business logic
 *      ↓
 * Repository
 *      ↓
 * Database
 *
 * AuthService hien tai xu ly:
 *
 * - Register.
 * - Login.
 * - Normalize du lieu.
 * - Kiem tra email trung.
 * - Hash password.
 * - Kiem tra password.
 * - Gan role USER khi register.
 */
@Service
public class AuthService {

    /*
     * Repository dung de truy cap bang users.
     */
    private final UserRepository userRepository;

    /*
     * PasswordEncoder duoc khai bao
     * trong PasswordConfig.
     *
     * Hien tai implementation la BCrypt.
     */
    private final PasswordEncoder passwordEncoder;

    /*
     * Constructor Injection.
     *
     * Spring se tu dong inject:
     *
     * - UserRepository
     * - PasswordEncoder
     *
     * vao AuthService.
     *
     * Khong can @Autowired vi class
     * chi co mot constructor.
     */
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /*
     * =========================================================
     * REGISTER
     * =========================================================
     *
     * Dang ky tai khoan moi.
     *
     * Luong:
     *
     * RegisterRequest
     *      ↓
     * normalize fullName
     *      ↓
     * normalize email
     *      ↓
     * kiem tra email ton tai
     *      ↓
     * BCrypt password
     *      ↓
     * Role.USER
     *      ↓
     * tao User
     *      ↓
     * save database
     *      ↓
     * AuthResponse
     *
     * @Transactional:
     *
     * Toan bo qua trinh register nam trong
     * mot database transaction.
     *
     * Neu viec luu database that bai,
     * transaction co the rollback.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        /*
         * Xoa khoang trang thua o dau va cuoi ho ten.
         *
         * Vi du:
         *
         * "  Nguyen Van A  "
         *
         * thanh:
         *
         * "Nguyen Van A"
         */
        String normalizedFullName =
                request.fullName().trim();

        /*
         * Chuan hoa email.
         *
         * Vi du:
         *
         * "  Student@Example.COM  "
         *
         * thanh:
         *
         * "student@example.com"
         *
         * Locale.ROOT giup lowercase khong
         * phu thuoc locale cua server.
         */
        String normalizedEmail =
                request.email()
                        .trim()
                        .toLowerCase(Locale.ROOT);

        /*
         * Kiem tra email da ton tai.
         *
         * Neu ton tai:
         *
         * throw EmailAlreadyExistsException
         *
         * GlobalExceptionHandler se chuyen thanh:
         *
         * HTTP 409 Conflict.
         */
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(
                    "Email is already registered"
            );
        }

        /*
         * Hash password bang BCrypt.
         *
         * request.password():
         * password goc nguoi dung gui len.
         *
         * passwordHash:
         * gia tri duoc luu vao database.
         *
         * Tuyet doi khong luu:
         *
         * request.password()
         *
         * truc tiep vao User entity.
         */
        String passwordHash =
                passwordEncoder.encode(request.password());

        /*
         * Tao User moi.
         *
         * Role.USER duoc backend tu dong gan.
         *
         * Frontend khong duoc phep quyet dinh
         * role trong API register.
         *
         * Dieu nay giup tranh privilege escalation.
         */
        User user = new User(
                normalizedFullName,
                normalizedEmail,
                passwordHash,
                Role.USER
        );

        /*
         * Luu User vao database.
         *
         * Sau khi persist:
         *
         * - id duoc tao.
         * - createdAt duoc gan.
         * - updatedAt duoc gan.
         */
        User savedUser =
                userRepository.save(user);

        /*
         * Khong tra User entity truc tiep.
         *
         * AuthResponse.from(...) chi expose
         * thong tin an toan cho frontend:
         *
         * - id
         * - fullName
         * - email
         *
         * passwordHash khong duoc gui ra ngoai.
         */
        return AuthResponse.from(savedUser);
    }

    /*
     * =========================================================
     * LOGIN
     * =========================================================
     *
     * Kiem tra email va password cua nguoi dung.
     *
     * Luong:
     *
     * LoginRequest
     *      ↓
     * normalize email
     *      ↓
     * findByEmail()
     *      ↓
     * user ton tai?
     *      ↓
     * BCrypt matches()
     *      ↓
     * password dung?
     *      ↓
     * User
     *
     * Method nay CHUA tao HttpSession.
     *
     * O checkpoint tiep theo:
     *
     * User
     *   ↓
     * Authentication
     *   ↓
     * SecurityContext
     *   ↓
     * HttpSession
     *   ↓
     * JSESSIONID
     */
    @Transactional(readOnly = true)
    public User login(LoginRequest request) {

        /*
         * Chuan hoa email giong nhu register.
         *
         * Vi du:
         *
         * "  Vana@Example.COM  "
         *
         * thanh:
         *
         * "vana@example.com"
         */
        String normalizedEmail =
                request.email()
                        .trim()
                        .toLowerCase(Locale.ROOT);

        /*
         * Tim User trong database.
         *
         * Neu email khong ton tai,
         * khong tra message:
         *
         * "Email does not exist"
         *
         * ma dung message chung:
         *
         * "Invalid email or password"
         *
         * de han che account enumeration.
         */
        User user = userRepository
                .findByEmail(normalizedEmail)
                .orElseThrow(
                        () -> new InvalidCredentialsException(
                                "Invalid email or password"
                        )
                );

        /*
         * So sanh password nguoi dung vua nhap
         * voi BCrypt hash dang luu trong database.
         *
         * BCrypt khong decrypt passwordHash.
         *
         * matches(...) se kiem tra:
         *
         * raw password
         *      ↓
         * BCrypt
         *      ↓
         * co khop passwordHash hay khong.
         */
        boolean passwordMatches =
                passwordEncoder.matches(
                        request.password(),
                        user.getPasswordHash()
                );

        /*
         * Neu password khong dung,
         * tra cung mot message voi truong hop
         * email khong ton tai.
         *
         * GlobalExceptionHandler se chuyen
         * InvalidCredentialsException thanh:
         *
         * HTTP 401 Unauthorized.
         */
        if (!passwordMatches) {
            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }

        /*
         * Den day co nghia:
         *
         * - Email ton tai.
         * - Password dung.
         *
         * User chi duoc tra noi bo cho Controller.
         *
         * Controller KHONG duoc return truc tiep
         * User entity ra frontend.
         *
         * Sau nay response se dung:
         *
         * AuthResponse.from(user)
         *
         * Sau do User cung se duoc dung de tao:
         *
         * SecurityContext + HttpSession.
         */
        return user;
    }
}
