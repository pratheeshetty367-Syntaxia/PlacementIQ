package com.placementiq.validation;
 
import com.placementiq.entity.Resume;
import com.placementiq.exception.AtsAnalysisException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
 
/**
 * Pre-flight checks that run before any OpenAI call is made.
 *
 * The Bean Validation annotations on AtsAnalysisRequest cover the shape of the
 * request body, but they cannot see the stored resume. These rules do: they
 * confirm the resume actually holds usable text, and stop obviously unusable
 * input from being sent upstream. Failing here costs nothing, whereas failing
 * after the API call costs a request, a quota unit and the student's time.
 */
@Component
public class AtsAnalysisValidator {
 
    /**
     * A genuine resume is comfortably longer than this. Below it the text is
     * almost certainly a failed extraction rather than a short resume.
     */
    private static final int MIN_RESUME_CHARACTERS = 150;
 
    /** A resume with fewer words than this cannot produce a meaningful analysis. */
    private static final int MIN_RESUME_WORDS = 30;
 
    /**
     * Confirms the stored resume can be analysed.
     *
     * @param resume the resume belonging to the authenticated user
     * @throws AtsAnalysisException when the document has no usable text
     */
    public void validateForAnalysis(Resume resume) {
        String text = resume.getExtractedText();
 
        if (!StringUtils.hasText(text)) {
            throw new AtsAnalysisException(
                    "No readable text could be extracted from your resume, so it cannot be analysed. "
                            + "This usually means the file is a scanned image. "
                            + "Please upload a text-based PDF or a DOCX file and try again.");
        }
 
        String trimmed = text.trim();
 
        if (trimmed.length() < MIN_RESUME_CHARACTERS) {
            throw new AtsAnalysisException(
                    "Your resume contains too little readable text to analyse ("
                            + trimmed.length() + " characters). "
                            + "Please upload a complete resume and try again.");
        }
 
        int wordCount = trimmed.split("\\s+").length;
        if (wordCount < MIN_RESUME_WORDS) {
            throw new AtsAnalysisException(
                    "Your resume contains only " + wordCount + " readable words, "
                            + "which is not enough for a meaningful analysis. "
                            + "Please upload a complete resume and try again.");
        }
    }
 
    /**
     * Normalises the optional target role: trims it, collapses internal runs of
     * whitespace, and turns a blank value into null so downstream code has a
     * single "not supplied" representation to check.
     */
    public String normalizeTargetRole(String targetRole) {
        if (!StringUtils.hasText(targetRole)) {
            return null;
        }
        return targetRole.trim().replaceAll("\\s+", " ");
    }
}
