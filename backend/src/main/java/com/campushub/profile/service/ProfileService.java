package com.campushub.profile.service;

import com.campushub.profile.dto.ProfileResponse;
import com.campushub.profile.dto.UpdateProfileRequest;
import com.campushub.security.UserPrincipal;

import java.util.UUID;

public interface ProfileService {

    /**
     * Updates profile PII by explicitly encrypting sensitive fields before persisting.
     *
     * @param userId  ID of the user updating their profile
     * @param request profile update payload containing optional new PII
     * @return updated ProfileResponse with decrypted PII
     */
    ProfileResponse updateProfile(UUID userId, UpdateProfileRequest request);

    /**
     * Retrieves profile. Decrypts PII only if requester is resource owner or ADMIN;
     * otherwise returns masked PII (e.g. 091****678) or null.
     *
     * @param targetUserId ID of the profile to retrieve
     * @param requester    security principal of the user making the request
     * @return ProfileResponse with decrypted or masked PII
     */
    ProfileResponse getProfile(UUID targetUserId, UserPrincipal requester);
}
