package com.placementiq.repository;
 
import com.placementiq.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.Optional;
 
/**
 * Data access layer for the Resume entity.
 *
 * All lookups are performed by the owning user's id (taken from the JWT),
 * never by the resume id supplied by the client, so a user can only ever
 * reach their own document.
 */
public interface ResumeRepository extends JpaRepository<Resume, Long> {
 
    Optional<Resume> findByUserId(Long userId);
 
    boolean existsByUserId(Long userId);
 
    void deleteByUserId(Long userId);
}
