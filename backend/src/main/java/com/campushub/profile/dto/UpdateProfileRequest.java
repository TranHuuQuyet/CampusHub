package com.campushub.profile.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 100, message = "Full name cannot exceed 100 characters")
        String fullName,

        @Size(max = 20, message = "Phone number cannot exceed 20 characters")
        String phoneNumber,

        @Size(max = 50, message = "Student card ID cannot exceed 50 characters")
        String studentCardId
) {
}
