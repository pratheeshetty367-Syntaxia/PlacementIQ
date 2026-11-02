package com.placementiq.service;
 
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.placementiq.config.OpenAiProperties;
import com.placementiq.dto.openai.OpenAiChatRequest;
import com.placementiq.dto.openai.OpenAiChatResponse;
import com.placementiq.dto.openai.OpenAiMessage;
import com.placementiq.exception.OpenAiServiceException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
 
import java.util.List;
import java.util.Map;
 
/**
 * The single, reusable gateway to the OpenAI Chat Completions API.
 *
 * Every future AI feature of PlacementIQ (job-description matching, interview
 * question generation, mock interviews) is meant to call this class rather than
 * building its own HTTP client, so authentication, timeouts, retries, logging
 * and error translation are implemented once.
 *
 * Nothing in this class knows anything about resumes or ATS scoring - it takes
 * a system prompt and a user prompt and gives back either raw text or a parsed
 * object. Keeping it feature-agnostic is what makes it reusable.
 *
 * Error handling contract: every failure leaves this class as an
 * OpenAiServiceException carrying the HTTP status PlacementIQ should return.
 * No RestClient exception is ever allowed to escape.
 */
@Service
@RequiredArgsConstructor
public class OpenAiService {
 
    private static final Logger log = LoggerFactory.getLogger(OpenAiService.class);
 
    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";
 
    private final RestClient openAiRestClient;
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;
 
    /**
     * Sends a two-message conversation and returns the assistant's raw reply.
     *
     * @param systemPrompt instructions that define the assistant's behaviour
     * @param userPrompt   the actual content to work on
     * @param jsonMode     when true, asks the API to guarantee valid JSON output
     * @return the assistant message content, never null or blank
     */
    public String complete(String systemPrompt, String userPrompt, boolean jsonMode) {
        requireConfiguredKey();
 
        OpenAiChatRequest request = OpenAiChatRequest.builder()
                .model(properties.getModel())
                .messages(List.of(
                        OpenAiMessage.system(systemPrompt),
                        OpenAiMessage.user(userPrompt)))
                .temperature(properties.getTemperature())
                .maxTokens(properties.getMaxTokens())
                .responseFormat(jsonMode ? Map.of("type", "json_object") : null)
                .build();
 
        OpenAiChatResponse response = callWithRetries(request);
        String content = response.firstMessageContent();
 
        if (!StringUtils.hasText(content)) {
            throw new OpenAiServiceException(HttpStatus.BAD_GATEWAY,
                    "The AI service returned an empty response. Please try again.");
        }
        return content;
    }
 
    /**
     * Sends a conversation in JSON mode and deserialises the reply into the
     * given type. This is the method feature services normally use.
     *
     * @param targetType the class describing the JSON schema the prompt asked for
     */
    public <T> T completeAsJson(String systemPrompt, String userPrompt, Class<T> targetType) {
        String content = complete(systemPrompt, userPrompt, true);
        try {
            return objectMapper.readValue(stripCodeFences(content), targetType);
        } catch (JsonProcessingException ex) {
            log.error("Could not parse the OpenAI JSON reply into {}: {}",
                    targetType.getSimpleName(), ex.getOriginalMessage());
            throw new OpenAiServiceException(HttpStatus.BAD_GATEWAY,
                    "The AI service returned a response that could not be read. Please try again.", ex);
        }
    }
 
    /**
     * @return true when an API key is configured, so callers (and a health page)
     *         can check availability without triggering a request.
     */
    public boolean isAvailable() {
        return properties.isConfigured();
    }
 
    /** @return the model name configured for this deployment, recorded on each analysis. */
    public String getConfiguredModel() {
        return properties.getModel();
    }
 
    // ------------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------------
 
    private void requireConfiguredKey() {
        if (!properties.isConfigured()) {
            log.error("OpenAI API key is not configured - set the OPENAI_API_KEY environment variable.");
            throw new OpenAiServiceException(HttpStatus.SERVICE_UNAVAILABLE,
                    "AI analysis is not configured on this server. "
                            + "Please set the OPENAI_API_KEY environment variable and restart the application.");
        }
    }
 
    /**
     * Performs the HTTP call, retrying only on failures that are plausibly
     * transient (timeouts, 429 rate limiting, 5xx). A 401 or 400 is a permanent
     * problem, so retrying it would only waste time and quota.
     */
    private OpenAiChatResponse callWithRetries(OpenAiChatRequest request) {
        int attempts = Math.max(1, properties.getMaxRetries() + 1);
        OpenAiServiceException lastFailure = null;
 
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                long startedAt = System.currentTimeMillis();
                OpenAiChatResponse response = openAiRestClient.post()
                        .uri(CHAT_COMPLETIONS_PATH)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                        .body(request)
                        .retrieve()
                        .body(OpenAiChatResponse.class);
 
                if (response == null) {
                    throw new OpenAiServiceException(HttpStatus.BAD_GATEWAY,
                            "The AI service returned no response body.");
                }
                log.debug("OpenAI call succeeded on attempt {} in {} ms (model={})",
                        attempt, System.currentTimeMillis() - startedAt, response.getModel());
                return response;
 
            } catch (ResourceAccessException ex) {
                // Connect or read timeout, DNS failure, connection reset.
                lastFailure = new OpenAiServiceException(HttpStatus.GATEWAY_TIMEOUT,
                        "The AI service did not respond in time. Please try again in a moment.", ex);
                log.warn("OpenAI attempt {}/{} timed out: {}", attempt, attempts, ex.getMessage());
 
            } catch (RestClientResponseException ex) {
                lastFailure = translateHttpError(ex);
                log.warn("OpenAI attempt {}/{} failed with HTTP {}", attempt, attempts, ex.getStatusCode().value());
                if (!isRetryable(ex.getStatusCode().value())) {
                    throw lastFailure;
                }
 
            } catch (OpenAiServiceException ex) {
                throw ex;
 
            } catch (RestClientException ex) {
                lastFailure = new OpenAiServiceException(HttpStatus.BAD_GATEWAY,
                        "The AI service could not be reached. Please try again.", ex);
                log.warn("OpenAI attempt {}/{} failed: {}", attempt, attempts, ex.getMessage());
            }
 
            if (attempt < attempts) {
                sleepBeforeRetry(attempt);
            }
        }
 
        throw (lastFailure != null) ? lastFailure : new OpenAiServiceException(
                HttpStatus.BAD_GATEWAY, "The AI service could not be reached. Please try again.");
    }
 
    /**
     * Maps an OpenAI HTTP status onto the status PlacementIQ returns, with a
     * message that is safe to show a student. The upstream body is logged but
     * never forwarded, because it can contain internal request details.
     */
    private OpenAiServiceException translateHttpError(RestClientResponseException ex) {
        int status = ex.getStatusCode().value();
        log.error("OpenAI responded with HTTP {}: {}", status, ex.getResponseBodyAsString());
 
        return switch (status) {
            case 401, 403 -> new OpenAiServiceException(HttpStatus.SERVICE_UNAVAILABLE,
                    "The AI service rejected the configured API key. "
                            + "Please check the OPENAI_API_KEY environment variable.", ex);
            case 429 -> new OpenAiServiceException(HttpStatus.SERVICE_UNAVAILABLE,
                    "The AI service is rate limited right now. Please try again in a minute.", ex);
            case 400 -> new OpenAiServiceException(HttpStatus.BAD_GATEWAY,
                    "The AI service rejected the analysis request.", ex);
            default -> new OpenAiServiceException(HttpStatus.BAD_GATEWAY,
                    "The AI service is temporarily unavailable. Please try again.", ex);
        };
    }
 
    private boolean isRetryable(int status) {
        return status == 429 || status >= 500;
    }
 
    /** Simple linear back-off; keeps a burst of retries from compounding the problem. */
    private void sleepBeforeRetry(int attempt) {
        try {
            Thread.sleep(attempt * 1000L);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new OpenAiServiceException(HttpStatus.SERVICE_UNAVAILABLE,
                    "The analysis was interrupted. Please try again.", ie);
        }
    }
 
    /**
     * Defensive cleanup: JSON mode should never wrap the reply in a markdown
     * code fence, but stripping one costs nothing and avoids a parse failure.
     */
    private String stripCodeFences(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline > -1) {
                trimmed = trimmed.substring(firstNewline + 1);
            }
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3);
            }
        }
        return trimmed.trim();
    }
}
