package com.campushub.config;

import com.campushub.common.security.RestAccessDeniedHandler;
import com.campushub.common.security.RestAuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import java.io.IOException;


// SecurityConfig
// │
// ├── NOW
// │   └── ① SecurityFilterChain      ← the only one you need today
// │           uses (injected, NOT declared here):
// │             • RestAuthenticationEntryPoint  (@Component)
// │             • RestAccessDeniedHandler       (@Component)
// │
// ├── LATER: register / login feature
// │   ├── ② PasswordEncoder           ← hashes passwords (BCrypt)
// │   └── ③ AuthenticationManager     ← checks email + password at login
// │
// └── LATER: when the frontend calls the API
//     └── ④ CorsConfigurationSource   ← allows the React/Vue origin

@Configuration
@EnableWebSecurity
public class securityConfig {
    //Step 1: Declare Security Filler Chain
    @Bean   
    public SecurityFilterChain securityFilterChain(HttpSecurity http
        ,RestAuthenticationEntryPoint authenticationEntryPoint
        ,RestAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        return http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        //rules in 
        .authorizeHttpRequests(auth -> auth 
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/health").permitAll()
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        )
        //exception handling    
        .exceptionHandling(ex-> ex
            .authenticationEntryPoint(authenticationEntryPoint)
            .accessDeniedHandler(accessDeniedHandler)
        )
        .build();
            
    }
    
}
