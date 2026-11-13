package com.campushub.profile.controller;

import com.campushub.common.exception.GlobalExceptionHandler;
import com.campushub.common.exception.ResourceNotFoundException;
import com.campushub.profile.dto.ProfileResponse;
import com.campushub.profile.dto.UpdateProfileRequest;
import com.campushub.profile.service.ProfileService;
import com.campushub.security.UserPrincipal;
import com.campushub.user.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock
    private ProfileService profileService;

    private MockMvc mockMvc;
    private UserPrincipal activePrincipal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ProfileController(profileService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType().isAssignableFrom(UserPrincipal.class);
                    }

                    @Override
                    public Object resolveArgument(
                            MethodParameter parameter,
                            ModelAndViewContainer mavContainer,
                            NativeWebRequest webRequest,
                            WebDataBinderFactory binderFactory
                    ) {
                        return activePrincipal;
                    }
                })
                .build();
    }

    @Test
    @DisplayName("PUT /api/v1/profile: Successfully updates profile with sensitive PII")
    void shouldUpdateProfileSuccessfully() throws Exception {
        UUID userId = UUID.randomUUID();
        activePrincipal = new UserPrincipal(userId, "alice@example.com", Role.USER);

        ProfileResponse mockResponse = new ProfileResponse(
                userId,
                "Alice Smith",
                "alice@example.com",
                Role.USER,
                "0912345678",
                "SV2026001",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(profileService.updateProfile(eq(userId), any(UpdateProfileRequest.class)))
                .thenReturn(mockResponse);

        String jsonPayload = """
                {
                    "fullName": "Alice Smith",
                    "phoneNumber": "0912345678",
                    "studentCardId": "SV2026001"
                }
                """;

        mockMvc.perform(put("/api/v1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.fullName").value("Alice Smith"))
                .andExpect(jsonPath("$.phoneNumber").value("0912345678"))
                .andExpect(jsonPath("$.studentCardId").value("SV2026001"));
    }

    @Test
    @DisplayName("PUT /api/v1/profile: Returns 401 when requester is unauthenticated")
    void shouldReturn401WhenUpdatingWithoutAuthentication() throws Exception {
        activePrincipal = null;

        String jsonPayload = """
                {
                    "fullName": "Alice Smith",
                    "phoneNumber": "0912345678"
                }
                """;

        mockMvc.perform(put("/api/v1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("GET /api/v1/profile/{id}: Successfully retrieves profile")
    void shouldGetProfileSuccessfully() throws Exception {
        UUID targetId = UUID.randomUUID();
        activePrincipal = new UserPrincipal(targetId, "alice@example.com", Role.USER);

        ProfileResponse mockResponse = new ProfileResponse(
                targetId,
                "Alice Smith",
                "alice@example.com",
                Role.USER,
                "0912345678",
                "SV2026001",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(profileService.getProfile(targetId, activePrincipal)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/profile/" + targetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(targetId.toString()))
                .andExpect(jsonPath("$.fullName").value("Alice Smith"))
                .andExpect(jsonPath("$.phoneNumber").value("0912345678"));
    }

    @Test
    @DisplayName("GET /api/v1/profile/{id}: Returns 404 when profile not found")
    void shouldReturn404WhenProfileNotFound() throws Exception {
        UUID targetId = UUID.randomUUID();
        activePrincipal = new UserPrincipal(UUID.randomUUID(), "other@example.com", Role.USER);

        when(profileService.getProfile(targetId, activePrincipal))
                .thenThrow(new ResourceNotFoundException("User not found with id: " + targetId));

        mockMvc.perform(get("/api/v1/profile/" + targetId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User not found with id: " + targetId));
    }
}
