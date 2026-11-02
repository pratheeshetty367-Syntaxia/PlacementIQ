package com.placementiq.dto;
 
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
/**
 * Optional request body for POST /api/ats/analyze.
 *
 * The module analyses the resume already stored in Part 2, so no file or text
 * is ever sent by the client. The only thing a student may supply is the role
 * they are targeting, which lets the analysis judge keyword coverage against
 * that role instead of generically. The field is validated with Bean Validation
 * so the existing MethodArgumentNotValidException handler reports problems in
 * the standard ApiError shape.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AtsAnalysisRequest {
 
    @Size(max = 150, message = "Target role must be at most 150 characters")
    @Pattern(
            regexp = "^[\\p{L}0-9 .,&/+#()-]*$",
            message = "Target role contains unsupported characters"
    )
    private String targetRole;
}
