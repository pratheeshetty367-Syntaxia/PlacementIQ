package com.placementiq.security;
 
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
 
import java.io.IOException;
 
/**
 * Runs once per request. Extracts the "Authorization: Bearer <token>" header,
 * validates the JWT, loads the corresponding user, and — if valid —
 * populates the SecurityContext so downstream controllers see an
 * authenticated principal.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
 
    private static final String HEADER_NAME = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
 
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
 
 
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = resolveToken(request);
 
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                String email = jwtUtil.getEmailFromToken(token);
 
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
 
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
 
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            // Do not throw - let the request continue unauthenticated;
            // Spring Security will reject it later if the endpoint requires auth.
            logger.error("Could not set user authentication in security context", ex);
        }
 
        filterChain.doFilter(request, response);
    }
 
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HEADER_NAME);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
 
