package com.campushub.user.repository;

import com.campushub.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/*
 * Repository quan ly viec truy cap du lieu cua User.
 *
 * JpaRepository<User, UUID> co nghia:
 *
 * User:
 * Entity ma repository nay quan ly.
 *
 * UUID:
 * Kieu du lieu cua khoa chinh User.id.
 *
 * Spring Data JPA se tu cung cap cac method co ban nhu:
 *
 * save(...)
 * findById(...)
 * findAll()
 * delete(...)
 * count()
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    /*
     * Tim user theo email.
     *
     * Spring Data JPA tu dong tao query dua vao ten method.
     *
     * Method nay gan tuong duong:
     *
     * SELECT *
     * FROM users
     * WHERE email = ?
     *
     * Optional<User> duoc dung vi co the:
     *
     * - Tim thay user
     * - Khong tim thay user
     *
     * Sau nay login se su dung method nay.
     */
    Optional<User> findByEmail(String email);

    /*
     * Kiem tra email da ton tai trong database hay chua.
     *
     * Ket qua:
     *
     * true  -> email da ton tai
     * false -> email chua ton tai
     *
     * Sau nay register se dung method nay de chan
     * hai tai khoan su dung cung mot email.
     */
    boolean existsByEmail(String email);
}
