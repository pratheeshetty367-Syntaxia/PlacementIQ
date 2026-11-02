package com.placementiq.config;
 
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
 
/**
 * Type-safe binding for the "placementiq.openai.*" block of application.yml,
 * which Part 1 already scaffolded with api-key and model entries.
 *
 * The api-key value in application.yml is written as ${OPENAI_API_KEY:} so the
 * real key is always supplied by an environment variable and never committed to
 * source control. The placeholder default is an empty string, which isConfigured()
 * detects so the application can fail with a clear message instead of sending an
 * unauthenticated request to OpenAI.
 */
@Component
@ConfigurationProperties(prefix = "placementiq.openai")
@Getter
@Setter
public class OpenAiProperties {
 
    /** Injected from the OPENAI_API_KEY environment variable. */
    private String apiKey = "";
 
    /** Chat Completions model, e.g. gpt-4o-mini. */
    private String model = "gpt-4o-mini";
 
    /** Base URL of the API; overridable for a proxy or a test double. */
    private String baseUrl = "https://api.openai.com/v1";
 
    /** Low temperature keeps the structured analysis stable between runs. */
    private double temperature = 0.2;
 
    private int maxTokens = 1800;
 
    /** TCP connect timeout in milliseconds. */
    private int connectTimeoutMs = 10_000;
 
    /** Read timeout in milliseconds - an analysis usually completes well inside this. */
    private int readTimeoutMs = 60_000;
 
    /** How many times a failed call is retried before giving up. */
    private int maxRetries = 2;
 
    /**
     * @return true when a usable API key has been supplied, i.e. the environment
     *         variable is set and is not the Part 1 placeholder value.
     */
    public boolean isConfigured() {
        return StringUtils.hasText(apiKey)
                && !apiKey.startsWith("REPLACE_WITH");
    }
}
