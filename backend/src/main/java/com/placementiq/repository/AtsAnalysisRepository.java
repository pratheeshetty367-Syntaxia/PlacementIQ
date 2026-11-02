package com.placementiq.repository;
 
import com.placementiq.entity.AtsAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
import java.util.Optional;
 
/**
 * Data access layer for stored ATS analyses.
 *
 * Every query is keyed on the owning user's id, which is taken from the JWT by
 * the controller. The findByIdAndUserId lookup exists so that even when the
 * client supplies an analysis id, the row is only returned when it actually
 * belongs to the caller - a user can never read another student's analysis.
 */
public interface AtsAnalysisRepository extends JpaRepository<AtsAnalysis, Long> {
 
    /** Newest analysis first - the React page shows element 0 by default. */
    List<AtsAnalysis> findByUserIdOrderByAnalyzedAtDesc(Long userId);
 
    Optional<AtsAnalysis> findFirstByUserIdOrderByAnalyzedAtDesc(Long userId);
 
    Optional<AtsAnalysis> findByIdAndUserId(Long id, Long userId);
 
    long countByUserId(Long userId);
 
    void deleteByResumeId(Long resumeId);
}
