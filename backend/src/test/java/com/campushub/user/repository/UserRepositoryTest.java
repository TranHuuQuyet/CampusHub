package com.campushub.user.repository;

import com.campushub.config.JpaConfig;
import com.campushub.user.model.Role;
import com.campushub.user.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should persist user, find by email, and auto-populate audit timestamps")
    void shouldSaveAndFindUserWithAuditTimestamps() {
        User user = new User("Alice Smith", "alice@example.com", "hashedPassword123", Role.USER);

        User savedUser = userRepository.saveAndFlush(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getFullName()).isEqualTo("Alice Smith");
        assertThat(savedUser.getEmail()).isEqualTo("alice@example.com");
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
        assertThat(savedUser.isEnabled()).isTrue();

        // Audit timestamps populated by BaseEntity and AuditingEntityListener
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isEqualTo(savedUser.getUpdatedAt());

        Optional<User> found = userRepository.findByEmail("alice@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(savedUser.getId());

        assertThat(userRepository.existsByEmail("alice@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("unknown@example.com")).isFalse();
    }

    @Test
    @DisplayName("Should throw DataIntegrityViolationException when duplicate email is inserted")
    void shouldThrowExceptionWhenDuplicateEmailIsInserted() {
        User user1 = new User("User One", "duplicate@example.com", "hash1", Role.USER);
        userRepository.saveAndFlush(user1);

        User user2 = new User("User Two", "duplicate@example.com", "hash2", Role.USER);

        assertThatThrownBy(() -> userRepository.saveAndFlush(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should update only updatedAt timestamp when user entity is modified")
    void shouldUpdateOnlyUpdatedAtTimestampOnModification() throws InterruptedException {
        User user = new User("Bob Jones", "bob@example.com", "hashBob", Role.USER);
        User savedUser = userRepository.saveAndFlush(user);

        var initialCreatedAt = savedUser.getCreatedAt();
        var initialUpdatedAt = savedUser.getUpdatedAt();

        Thread.sleep(50);

        savedUser.setFullName("Bob Jones Jr.");
        User updatedUser = userRepository.saveAndFlush(savedUser);

        assertThat(updatedUser.getCreatedAt()).isEqualTo(initialCreatedAt);
        assertThat(updatedUser.getUpdatedAt()).isAfter(initialUpdatedAt);
    }
}
