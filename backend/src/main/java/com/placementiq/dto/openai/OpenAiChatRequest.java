package com.placementiq.dto.openai;
 
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
import java.util.List;
import java.util.Map;
 
/**
 * Request body for POST {openai-base-url}/chat/completions.
 *
 * Field names that differ from Java conventions are mapped with @JsonProperty
 * (max_tokens, response_format) and nulls are omitted, so optional parameters
 * are simply left out of the payload instead of being sent as null.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenAiChatRequest {
 
    private String model;
 
    private List<OpenAiMessage> messages;
 
    private Double temperature;
 
    @JsonProperty("max_tokens")
    private Integer maxTokens;
 
    /**
     * Asks the API to guarantee syntactically valid JSON in the reply,
     * i.e. { "type": "json_object" }. This is what makes parsing the
     * analysis reliable instead of best-effort string scraping.
     */
    @JsonProperty("response_format")
    private Map<String, String> responseFormat;
}
