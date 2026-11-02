package com.placementiq.repository;
 
import com.placementiq.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.Optional;
 
/**
 * Data access layer for the UserProfile entity.
 */
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
 
    Optional<UserProfile> findByUserId(Long userId);
}
 
