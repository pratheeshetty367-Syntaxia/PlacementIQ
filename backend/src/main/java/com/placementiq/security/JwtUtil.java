package com.placementiq.security;
 
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
 
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;
 
/**
 * Handles everything related to JSON Web Tokens:
 * generation of tokens on successful login/registration, and
 * validation/parsing of tokens on every authenticated request.
 */
@Component
public class JwtUtil {
 
    @Value("${placementiq.jwt.secret}")
    private String jwtSecret;
 
    @Value("${placementiq.jwt.expiration-ms}")
    private long jwtExpirationMs;
 
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
 
    /**
     * Generates a signed JWT for the given authenticated principal.
     */
    public String generateToken(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
 
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
 
        return Jwts.builder()
                .subject(principal.getEmail())
                .claim("userId", principal.getId())
                .claim("fullName", principal.getFullName())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
 
    public String generateTokenFromEmail(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
 
        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
 
    public String getEmailFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }
 
    public Date getExpirationFromToken(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
 
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
 
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
 
    /**
     * Validates the signature and expiration of a JWT.
     * Returns false (rather than throwing) so filters can handle
     * invalid tokens gracefully.
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (SignatureException ex) {
            System.err.println("Invalid JWT signature: " + ex.getMessage());
        } catch (MalformedJwtException ex) {
            System.err.println("Invalid JWT token: " + ex.getMessage());
        } catch (ExpiredJwtException ex) {
            System.err.println("Expired JWT token: " + ex.getMessage());
        } catch (UnsupportedOperationException ex) {
            System.err.println("Unsupported JWT token: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            System.err.println("JWT claims string is empty: " + ex.getMessage());
        }
        return false;
    }
}
 
