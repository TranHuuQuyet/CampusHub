package com.campushub.profile.dto;

import com.campushub.user.model.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProfileResponse(
        UUID id,
        String fullName,
        String email,
        Role role,
        String phoneNumber,
        String studentCardId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
