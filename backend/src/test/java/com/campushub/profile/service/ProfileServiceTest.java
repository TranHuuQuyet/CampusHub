package com.campushub.profile.service;

import com.campushub.common.exception.ResourceNotFoundException;
import com.campushub.common.security.crypto.AesGcmCryptoService;
import com.campushub.profile.dto.ProfileResponse;
import com.campushub.profile.dto.UpdateProfileRequest;
import com.campushub.security.UserPrincipal;
import com.campushub.user.model.Role;
import com.campushub.user.model.User;
import com.campushub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AesGcmCryptoService cryptoService;

    @InjectMocks
    private ProfileServiceImpl profileService;

    private UUID userId;
    private User testUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = new User("Alice Smith", "alice@example.com", "hashedPassword", Role.USER);
        testUser.setId(userId);
    }

    @Test
    @DisplayName("AC-4: Explicit service-layer encryption of phoneNumber and studentCardId before repository save")
    void shouldExplicitlyEncryptPiiBeforeSavingProfile() {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "Alice Johnson",
                "0912345678",
                "SV2026001"
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(cryptoService.encrypt("0912345678")).thenReturn("CIPHERTEXT_PHONE");
        when(cryptoService.encrypt("SV2026001")).thenReturn("CIPHERTEXT_CARD");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cryptoService.decrypt("CIPHERTEXT_PHONE")).thenReturn("0912345678");
        when(cryptoService.decrypt("CIPHERTEXT_CARD")).thenReturn("SV2026001");

        ProfileResponse response = profileService.updateProfile(userId, request);

        // Verify explicit service-layer encryption calls
        verify(cryptoService).encrypt("0912345678");
        verify(cryptoService).encrypt("SV2026001");

        // Verify entity received ciphertext before save
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertThat(capturedUser.getFullName()).isEqualTo("Alice Johnson");
        assertThat(capturedUser.getEncryptedPhoneNumber()).isEqualTo("CIPHERTEXT_PHONE");
        assertThat(capturedUser.getEncryptedStudentCardId()).isEqualTo("CIPHERTEXT_CARD");

        // Verify response contains plain text for owner
        assertThat(response.fullName()).isEqualTo("Alice Johnson");
        assertThat(response.phoneNumber()).isEqualTo("0912345678");
        assertThat(response.studentCardId()).isEqualTo("SV2026001");
    }

    @Test
    @DisplayName("AC-5: Authorized requester (Resource Owner) decrypts sensitive PII")
    void shouldDecryptPiiWhenRequesterIsResourceOwner() {
        testUser.setEncryptedPhoneNumber("CIPHERTEXT_PHONE");
        testUser.setEncryptedStudentCardId("CIPHERTEXT_CARD");

        UserPrincipal ownerPrincipal = new UserPrincipal(userId, "alice@example.com", Role.USER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(cryptoService.decrypt("CIPHERTEXT_PHONE")).thenReturn("0912345678");
        when(cryptoService.decrypt("CIPHERTEXT_CARD")).thenReturn("SV2026001");

        ProfileResponse response = profileService.getProfile(userId, ownerPrincipal);

        verify(cryptoService).decrypt("CIPHERTEXT_PHONE");
        verify(cryptoService).decrypt("CIPHERTEXT_CARD");
        assertThat(response.phoneNumber()).isEqualTo("0912345678");
        assertThat(response.studentCardId()).isEqualTo("SV2026001");
    }

    @Test
    @DisplayName("AC-5: Authorized requester (ADMIN) decrypts sensitive PII")
    void shouldDecryptPiiWhenRequesterIsAdmin() {
        UUID adminId = UUID.randomUUID();
        UserPrincipal adminPrincipal = new UserPrincipal(adminId, "admin@example.com", Role.ADMIN);

        testUser.setEncryptedPhoneNumber("CIPHERTEXT_PHONE");
        testUser.setEncryptedStudentCardId("CIPHERTEXT_CARD");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(cryptoService.decrypt("CIPHERTEXT_PHONE")).thenReturn("0912345678");
        when(cryptoService.decrypt("CIPHERTEXT_CARD")).thenReturn("SV2026001");

        ProfileResponse response = profileService.getProfile(userId, adminPrincipal);

        verify(cryptoService).decrypt("CIPHERTEXT_PHONE");
        verify(cryptoService).decrypt("CIPHERTEXT_CARD");
        assertThat(response.phoneNumber()).isEqualTo("0912345678");
        assertThat(response.studentCardId()).isEqualTo("SV2026001");
    }

    @Test
    @DisplayName("AC-5: Unauthorized / Public requester does NOT trigger decryption and receives masked PII")
    void shouldMaskPiiAndNeverDecryptForPublicRequester() {
        UUID otherUserId = UUID.randomUUID();
        UserPrincipal otherUserPrincipal = new UserPrincipal(otherUserId, "bob@example.com", Role.USER);

        testUser.setEncryptedPhoneNumber("CIPHERTEXT_PHONE");
        testUser.setEncryptedStudentCardId("CIPHERTEXT_CARD");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        ProfileResponse response = profileService.getProfile(userId, otherUserPrincipal);

        // Security requirement: Decryption engine must NEVER be invoked for public queries
        verify(cryptoService, never()).decrypt(anyString());

        assertThat(response.phoneNumber()).isEqualTo("091****678");
        assertThat(response.studentCardId()).isEqualTo("******");
    }

    @Test
    @DisplayName("AC-5: Unauthenticated requester (null principal) receives masked PII without decryption")
    void shouldMaskPiiForAnonymousRequester() {
        testUser.setEncryptedPhoneNumber("CIPHERTEXT_PHONE");
        testUser.setEncryptedStudentCardId("CIPHERTEXT_CARD");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        ProfileResponse response = profileService.getProfile(userId, null);

        verify(cryptoService, never()).decrypt(anyString());
        assertThat(response.phoneNumber()).isEqualTo("091****678");
        assertThat(response.studentCardId()).isEqualTo("******");
    }

    @Test
    @DisplayName("Public requester receives null when user has no PII set")
    void shouldReturnNullWhenUserHasNoPii() {
        testUser.setEncryptedPhoneNumber(null);
        testUser.setEncryptedStudentCardId(null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        ProfileResponse response = profileService.getProfile(userId, null);

        verify(cryptoService, never()).decrypt(anyString());
        assertThat(response.phoneNumber()).isNull();
        assertThat(response.studentCardId()).isNull();
    }

    @Test
    @DisplayName("Throws ResourceNotFoundException when profile does not exist")
    void shouldThrowExceptionWhenUserNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getProfile(nonExistentId, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: " + nonExistentId);
    }
}
