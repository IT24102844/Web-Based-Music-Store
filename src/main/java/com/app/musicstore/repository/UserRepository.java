package com.app.musicstore.repository;

import com.app.musicstore.model.Role;
import com.app.musicstore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Count users by role
    long countByRole(Role role);

    // Find user by email (JPQL)
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    // Check if a user with given email exists
    boolean existsByEmail(String email);

    // Find all users by role (JPQL)
    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findByRole(@Param("role") Role role);

    // Optional: Native query versions (if needed for specific DB tuning)
    @Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
    Optional<User> findByEmailNative(@Param("email") String email);

    @Query(value = "SELECT * FROM users WHERE role = :role", nativeQuery = true)
    List<User> findByRoleNative(@Param("role") String role);
}
