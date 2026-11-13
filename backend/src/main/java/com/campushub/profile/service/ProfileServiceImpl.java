package com.campushub.profile.service;

import com.campushub.common.exception.ResourceNotFoundException;
import com.campushub.common.security.crypto.AesGcmCryptoService;
import com.campushub.profile.dto.ProfileResponse;
import com.campushub.profile.dto.UpdateProfileRequest;
import com.campushub.security.UserPrincipal;
import com.campushub.user.model.User;
import com.campushub.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final AesGcmCryptoService cryptoService;

    public ProfileServiceImpl(UserRepository userRepository, AesGcmCryptoService cryptoService) {
        this.userRepository = userRepository;
        this.cryptoService = cryptoService;
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName().trim());
        }

        // Explicit service-layer symmetric encryption before persisting to database
        if (request.phoneNumber() != null) {
            user.setEncryptedPhoneNumber(cryptoService.encrypt(request.phoneNumber()));
        }

        if (request.studentCardId() != null) {
            user.setEncryptedStudentCardId(cryptoService.encrypt(request.studentCardId()));
        }

        User savedUser = userRepository.save(user);

        // Decrypt values for the owner's immediate response
        String plainPhone = cryptoService.decrypt(savedUser.getEncryptedPhoneNumber());
        String plainStudentCardId = cryptoService.decrypt(savedUser.getEncryptedStudentCardId());

        return toProfileResponse(savedUser, plainPhone, plainStudentCardId);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID targetUserId, UserPrincipal requester) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + targetUserId));

        boolean isAuthorized = requester != null && requester.isOwnerOrAdmin(targetUserId);

        if (isAuthorized) {
            // Authorized caller (Owner or ADMIN): explicitly decrypt sensitive PII
            String plainPhone = cryptoService.decrypt(user.getEncryptedPhoneNumber());
            String plainStudentCardId = cryptoService.decrypt(user.getEncryptedStudentCardId());
            return toProfileResponse(user, plainPhone, plainStudentCardId);
        } else {
            // Public / unauthorized caller: mask or omit PII without decrypting
            String maskedPhone = maskPhoneNumber(user.getEncryptedPhoneNumber());
            String maskedStudentCardId = maskStudentCardId(user.getEncryptedStudentCardId());
            return toProfileResponse(user, maskedPhone, maskedStudentCardId);
        }
    }

    private ProfileResponse toProfileResponse(User user, String phoneNumber, String studentCardId) {
        return new ProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                phoneNumber,
                studentCardId,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private String maskPhoneNumber(String encryptedPhoneNumber) {
        if (encryptedPhoneNumber == null || encryptedPhoneNumber.isBlank()) {
            return null;
        }
        return "091****678";
    }

    private String maskStudentCardId(String encryptedStudentCardId) {
        if (encryptedStudentCardId == null || encryptedStudentCardId.isBlank()) {
            return null;
        }
        return "******";
    }
}
