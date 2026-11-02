package com.placementiq.dto.openai;
 
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
import java.util.ArrayList;
import java.util.List;
 
/**
 * Java binding for the JSON object the language model is instructed to return.
 *
 * The exact field names here must match the schema described in
 * AtsPromptBuilder - that prompt and this class are two halves of one contract.
 * Unknown properties are ignored so an extra field in the model's reply can
 * never fail the whole analysis.
 *
 * This is an internal type: it is mapped onto the AtsAnalysis entity and then
 * onto AtsAnalysisDto, and is never returned to the browser directly.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AtsModelResult {
 
    private Integer overallScore;
    private Integer skillsScore;
    private Integer keywordScore;
    private Integer formattingScore;
    private Integer completenessScore;
 
    private List<String> skills = new ArrayList<>();
    private List<String> keywords = new ArrayList<>();
    private List<String> missingKeywords = new ArrayList<>();
    private List<String> suggestions = new ArrayList<>();
    private List<String> missingSections = new ArrayList<>();
 
    private List<Education> education = new ArrayList<>();
    private List<Experience> experience = new ArrayList<>();
 
    private Double totalExperienceYears;
    private String summary;
 
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Education {
        private String degree;
        private String institution;
        private String year;
        private String score;
    }
 
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Experience {
        private String jobTitle;
        private String organization;
        private String duration;
        private String description;
    }
}
