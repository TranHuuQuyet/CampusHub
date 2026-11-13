package com.campushub.user.model;

import com.campushub.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "encrypted_phone_number", length = 512)
    private String encryptedPhoneNumber;

    @Column(name = "encrypted_student_card_id", length = 512)
    private String encryptedStudentCardId;

    protected User() {
    }

    public User(String fullName, String email, String passwordHash, Role role) {
        this(fullName, email, passwordHash, role, true);
    }

    public User(String fullName, String email, String passwordHash, Role role, boolean enabled) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.enabled = enabled;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEncryptedPhoneNumber() {
        return encryptedPhoneNumber;
    }

    public void setEncryptedPhoneNumber(String encryptedPhoneNumber) {
        this.encryptedPhoneNumber = encryptedPhoneNumber;
    }

    public String getEncryptedStudentCardId() {
        return encryptedStudentCardId;
    }

    public void setEncryptedStudentCardId(String encryptedStudentCardId) {
        this.encryptedStudentCardId = encryptedStudentCardId;
    }
}
