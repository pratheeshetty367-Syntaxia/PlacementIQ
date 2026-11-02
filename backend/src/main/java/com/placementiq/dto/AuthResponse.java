package com.placementiq.dto;
 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
import java.util.List;
 
/**
 * Response payload returned to the client after successful
 * registration or login. Carries the JWT to be stored client-side
 * (e.g. localStorage) and attached to subsequent requests.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
 
    private String token;
 
    @Builder.Default
    private String tokenType = "Bearer";
 
    private Long userId;
 
    private String fullName;
 
    private String email;
 
    private List<String> roles;
}
 
