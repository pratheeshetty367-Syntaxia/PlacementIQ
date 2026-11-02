package com.placementiq.dto.openai;
 
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
/**
 * A single message in an OpenAI Chat Completions conversation.
 * role is one of "system", "user" or "assistant".
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpenAiMessage {
 
    private String role;
    private String content;
 
    public static OpenAiMessage system(String content) {
        return new OpenAiMessage("system", content);
    }
 
    public static OpenAiMessage user(String content) {
        return new OpenAiMessage("user", content);
    }
}
