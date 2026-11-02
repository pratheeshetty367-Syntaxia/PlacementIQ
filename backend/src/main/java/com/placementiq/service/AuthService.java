package com.placementiq.service;
 
import com.placementiq.dto.AuthResponse;
import com.placementiq.dto.LoginRequest;
import com.placementiq.dto.RegisterRequest;
import com.placementiq.entity.Role;
import com.placementiq.entity.User;
import com.placementiq.exception.ApiException;
import com.placementiq.repository.RoleRepository;
import com.placementiq.repository.UserRepository;
import com.placementiq.security.JwtUtil;
import com.placementiq.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
 
/**
 * Encapsulates the business logic for user registration and login:
 * password encoding, role assignment, authentication, and JWT issuance.
 */
@Service
@RequiredArgsConstructor
public class AuthService {
 
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
 
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }
 
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(null, Role.RoleName.ROLE_USER)));
 
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .roles(Set.of(userRole))
                .build();
 
        User savedUser = userRepository.save(user);
 
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new UserPrincipal(savedUser), null, new UserPrincipal(savedUser).getAuthorities());
 
        String token = jwtUtil.generateToken(authentication);
 
        return buildAuthResponse(savedUser, token);
    }
 
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
 
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
 
        String token = jwtUtil.generateToken(authentication);
 
        return buildAuthResponse(user, token);
    }
 
    private AuthResponse buildAuthResponse(User user, String token) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());
 
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }
}
 
