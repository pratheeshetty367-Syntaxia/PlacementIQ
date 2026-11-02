package com.placementiq.dto;
 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
import java.time.LocalDateTime;
 
/**
 * Lightweight row used by the analysis history endpoint.
 *
 * Returning the full AtsAnalysisDto for every past run would ship several
 * hundred list items to the browser just to draw a list of dates and scores,
 * so the history endpoint returns this compact shape instead.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtsAnalysisSummaryDto {
 
    private Long id;
    private String resumeFileName;
    private String targetRole;
    private Integer overallScore;
    private String scoreBand;
    private int skillCount;
    private int missingKeywordCount;
    private LocalDateTime analyzedAt;
}
