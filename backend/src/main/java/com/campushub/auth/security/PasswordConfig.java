package com.campushub.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/*
 * Cau hinh cac thanh phan lien quan den password
 * cho Authentication cua CampusHub.
 */
@Configuration
public class PasswordConfig {

    /*
     * Tao PasswordEncoder de cac Service co the su dung.
     *
     * @Bean:
     * Spring se tao va quan ly object PasswordEncoder nay.
     *
     * Sau nay trong AuthService ta co the inject:
     *
     * private final PasswordEncoder passwordEncoder;
     *
     * ma khong can tu:
     *
     * new BCryptPasswordEncoder()
     */
    @Bean
    PasswordEncoder passwordEncoder() {

        /*
         * BCrypt la thuat toan hash password.
         *
         * Vi du:
         *
         * Password nguoi dung:
         *   MyPassword123
         *
         * Sau khi encode:
         *   $2a$10$...
         *
         * Database chi luu chuoi hash.
         *
         * BCrypt co salt nen cung mot password
         * co the tao ra hai hash khac nhau.
         */
        return new BCryptPasswordEncoder();
    }
}
