package com.placementiq.dto.openai;
 
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
import java.util.List;
 
/**
 * Response body of the OpenAI Chat Completions endpoint.
 *
 * Only the fields PlacementIQ actually uses are declared;
 * @JsonIgnoreProperties(ignoreUnknown = true) means the API can add new fields
 * without breaking deserialisation here.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAiChatResponse {
 
    private String id;
    private String model;
    private List<Choice> choices;
    private Usage usage;
 
    /** Convenience accessor for the assistant text of the first choice. */
    public String firstMessageContent() {
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        Choice choice = choices.get(0);
        return (choice == null || choice.getMessage() == null) ? null : choice.getMessage().getContent();
    }
 
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        private Integer index;
        private OpenAiMessage message;
 
        @JsonProperty("finish_reason")
        private String finishReason;
    }
 
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {
 
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;
 
        @JsonProperty("completion_tokens")
        private Integer completionTokens;
 
        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}
