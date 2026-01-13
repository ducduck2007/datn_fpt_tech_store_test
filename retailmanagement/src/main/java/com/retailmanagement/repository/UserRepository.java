package com.retailmanagement.repository;

import com.retailmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    boolean existsByUsernameAndIdNot(String username, Integer id);
    boolean existsByEmailAndIdNot(String email, Integer id);

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    // login bằng username hoặc email
    Optional<User> findByUsernameOrEmail(String username, String email);
}
