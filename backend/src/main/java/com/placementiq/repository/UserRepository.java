package com.placementiq.repository;
 
import com.placementiq.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.Optional;
 
/**
 * Data access layer for the User entity.
 * Extends JpaRepository to get CRUD operations for free.
 */
public interface UserRepository extends JpaRepository<User, Long> {
 
    Optional<User> findByEmail(String email);
 
    boolean existsByEmail(String email);
}
 
