package com.placementiq.repository;
 
import com.placementiq.entity.PlacementProgress;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
 
/**
 * Data access layer for PlacementProgress records.
 */
public interface PlacementProgressRepository extends JpaRepository<PlacementProgress, Long> {
 
    List<PlacementProgress> findByUserId(Long userId);
 
    List<PlacementProgress> findByUserIdAndModuleName(Long userId, String moduleName);
}
 
