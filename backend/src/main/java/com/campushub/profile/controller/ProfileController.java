package com.campushub.profile.controller;

import com.campushub.common.exception.UnauthorizedException;
import com.campushub.profile.dto.ProfileResponse;
import com.campushub.profile.dto.UpdateProfileRequest;
import com.campushub.profile.service.ProfileService;
import com.campushub.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal requester,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        if (requester == null) {
            throw new UnauthorizedException("Authentication required to update profile");
        }
        ProfileResponse response = profileService.updateProfile(requester.getId(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponse> getProfile(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal requester
    ) {
        ProfileResponse response = profileService.getProfile(id, requester);
        return ResponseEntity.ok(response);
    }
}
