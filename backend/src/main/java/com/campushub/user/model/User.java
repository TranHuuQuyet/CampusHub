package com.campushub.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/*
 * Entity dai dien cho mot nguoi dung trong he thong CampusHub.
 *
 * Moi object User se duoc JPA/Hibernate map thanh mot record
 * trong bang "users" cua database.
 */
@Entity
@Table(name = "users")
public class User {

    /*
     * Khoa chinh cua user.
     *
     * UUID duoc tao tu dong khi luu user vao database.
     *
     * Vi du:
     * 550e8400-e29b-41d4-a716-446655440000
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /*
     * Ho va ten cua nguoi dung.
     *
     * nullable = false:
     * Khong duoc phep NULL trong database.
     *
     * length = 100:
     * Gioi han toi da 100 ky tu.
     */
    @Column(nullable = false, length = 100)
    private String fullName;

    /*
     * Email dung de dang ky va dang nhap.
     *
     * unique = true:
     * Hai user khong duoc su dung cung mot email.
     */
    @Column(nullable = false, unique = true, length = 254)
    private String email;

    /*
     * Password sau khi da duoc hash bang BCrypt.
     *
     * Tuyet doi KHONG luu password goc vao database.
     *
     * Vi du database se luu:
     * $2a$10$...
     *
     * Chu khong luu:
     * 12345678
     */
    @Column(nullable = false)
    private String passwordHash;

    /*
     * Quyen cua nguoi dung trong he thong.
     *
     * EnumType.STRING giup database luu:
     * USER
     * ADMIN
     *
     * thay vi luu cac so nhu 0, 1.
     *
     * User moi mac dinh co quyen USER.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    /*
     * Thoi diem tai khoan duoc tao.
     *
     * updatable = false:
     * Sau khi tao thi gia tri nay khong duoc thay doi.
     */
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /*
     * Thoi diem user duoc cap nhat gan nhat.
     */
    @Column(nullable = false)
    private Instant updatedAt;

    /*
     * Constructor rong bat buoc cho JPA/Hibernate.
     *
     * protected de han che code ben ngoai tao User rong
     * nhung Hibernate van co the su dung constructor nay.
     */
    protected User() {
    }

    /*
     * Constructor dung khi backend tao user moi.
     *
     * id khong can truyen vao vi database/JPA se tu sinh UUID.
     *
     * createdAt va updatedAt cung khong can truyen vao
     * vi @PrePersist se tu dong gan gia tri.
     */
    public User(
            String fullName,
            String email,
            String passwordHash,
            Role role
    ) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    /*
     * Duoc JPA tu dong goi truoc khi INSERT user moi vao database.
     *
     * Khi tao user:
     * createdAt = thoi gian hien tai
     * updatedAt = thoi gian hien tai
     */
    @PrePersist
    void onCreate() {
        Instant now = Instant.now();

        createdAt = now;
        updatedAt = now;
    }

    /*
     * Duoc JPA tu dong goi truoc khi UPDATE user trong database.
     *
     * Moi lan user thay doi thong tin,
     * updatedAt se duoc cap nhat.
     */
    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    // Lay ID cua user.
    public UUID getId() {
        return id;
    }

    // Lay ho ten user.
    public String getFullName() {
        return fullName;
    }

    // Lay email user.
    public String getEmail() {
        return email;
    }

    // Lay password da hash.
    public String getPasswordHash() {
        return passwordHash;
    }

    // Lay role hien tai cua user.
    public Role getRole() {
        return role;
    }

    // Lay thoi diem tai khoan duoc tao.
    public Instant getCreatedAt() {
        return createdAt;
    }

    // Lay thoi diem cap nhat gan nhat.
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // Cap nhat ho ten user.
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    // Cap nhat email user.
    public void setEmail(String email) {
        this.email = email;
    }

    // Cap nhat password hash.
    // Sau nay chi truyen password da duoc BCrypt hash vao day.
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /*
     * Cap nhat role.
     *
     * Can than khi su dung method nay.
     * API register binh thuong KHONG duoc cho phep user
     * tu chon ADMIN.
     */
    public void setRole(Role role) {
        this.role = role;
    }
}
