package com.placementiq.util;
 
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
 
/**
 * Builds the two prompts sent to the language model for an ATS analysis.
 *
 * The prompts are kept in their own class, out of the service, for three
 * reasons: they are long enough to bury the business logic if inlined, they are
 * the part most likely to be tuned while improving results, and the JSON schema
 * described here must stay in lock-step with AtsModelResult - keeping the two
 * side by side in the codebase makes that dependency obvious.
 */
@Component
public class AtsPromptBuilder {
 
    /** Hard cap on the resume text sent upstream, to bound token usage and cost. */
    private static final int MAX_RESUME_CHARACTERS = 12_000;
 
    /**
     * The system prompt: defines the assistant's role and the exact JSON schema
     * it must return. Every scoring instruction is explicit so the numbers stay
     * comparable between runs.
     */
    public String buildSystemPrompt() {
        return """
                You are an experienced technical recruiter and resume reviewer who helps
                engineering students prepare resumes that pass Applicant Tracking Systems.
 
                You will be given the plain text of one resume. Analyse it and reply with a
                single JSON object and nothing else - no explanation, no markdown fences.
 
                The JSON object must have exactly these keys:
                {
                  "overallScore": integer 0-100,
                  "skillsScore": integer 0-100,
                  "keywordScore": integer 0-100,
                  "formattingScore": integer 0-100,
                  "completenessScore": integer 0-100,
                  "skills": [string],
                  "keywords": [string],
                  "missingKeywords": [string],
                  "suggestions": [string],
                  "missingSections": [string],
                  "education": [{"degree": string, "institution": string, "year": string, "score": string}],
                  "experience": [{"jobTitle": string, "organization": string, "duration": string, "description": string}],
                  "totalExperienceYears": number,
                  "summary": string
                }
 
                Scoring guidance:
                - skillsScore: breadth, relevance and clarity of the technical skills listed.
                - keywordScore: how well the resume covers the keywords a recruiter or ATS
                  would search for, for the role it is aimed at.
                - formattingScore: ATS readability - clear standard section headings, plain
                  text that parses cleanly, consistent dates, no tables/columns/graphics
                  implied by the text, contact details present.
                - completenessScore: whether the standard sections are all present, namely
                  contact information, education, skills, experience or projects, and
                  optionally certifications and achievements.
                - overallScore: your holistic judgement across the four scores above. Do not
                  simply average them; weight keyword coverage and completeness most heavily.
 
                Content rules:
                - "skills": only skills actually stated in the resume. Do not invent any.
                - "keywords": ATS-relevant terms that ARE present in the resume.
                - "missingKeywords": 5 to 12 terms a recruiter would expect for the target
                  role but which are absent. These are recommendations, not facts about
                  the candidate.
                - "suggestions": 4 to 8 specific, actionable rewrites or additions. Each one
                  must name what to change and why it helps ATS parsing or recruiter review.
                  Avoid generic advice such as "improve your resume".
                - "missingSections": standard sections that appear to be absent entirely.
                - "education" and "experience": extract only what the resume states. Use an
                  empty string for a field the resume does not mention, and an empty array
                  if the whole section is absent. Include internships and significant
                  academic projects under "experience".
                - "totalExperienceYears": your best estimate from the dates given, as a
                  number. Use 0 when the resume shows no professional experience.
                - "summary": two or three sentences in plain language describing the overall
                  state of the resume and the single most valuable improvement.
 
                Never fabricate qualifications, employers, institutions or dates that are not
                in the text. If the text is too sparse to judge a dimension, score it
                conservatively and say so in the summary.
                """;
    }
 
    /**
     * The user prompt: the resume text itself plus the optional target role.
     *
     * @param resumeText plain text extracted from the stored resume in Part 2
     * @param targetRole the role the student is aiming for, may be null/blank
     */
    public String buildUserPrompt(String resumeText, String targetRole) {
        String role = StringUtils.hasText(targetRole)
                ? targetRole.trim()
                : "a general entry-level software engineering role";
 
        String text = resumeText.length() > MAX_RESUME_CHARACTERS
                ? resumeText.substring(0, MAX_RESUME_CHARACTERS)
                : resumeText;
 
        return """
                Target role: %s
 
                Analyse the following resume text for that target role and return the JSON
                object described in your instructions.
 
                --- BEGIN RESUME TEXT ---
                %s
                --- END RESUME TEXT ---
                """.formatted(role, text);
    }
}
