package com.placementiq.service;
 
import com.placementiq.dto.UserProfileDto;
import com.placementiq.entity.User;
import com.placementiq.entity.UserProfile;
import com.placementiq.exception.ResourceNotFoundException;
import com.placementiq.repository.UserProfileRepository;
import com.placementiq.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
/**
 * Business logic for fetching and updating the authenticated user's profile.
 */
@Service
@RequiredArgsConstructor
public class UserService {
 
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
 
    @Transactional(readOnly = true)
    public UserProfileDto getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
 
        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
 
        return mapToDto(user, profile);
    }
 
    @Transactional
    public UserProfileDto updateProfile(Long userId, UserProfileDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
 
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> UserProfile.builder().user(user).build());
 
        profile.setCollege(dto.getCollege());
        profile.setBranch(dto.getBranch());
        profile.setGraduationYear(dto.getGraduationYear());
        profile.setSkills(dto.getSkills());
        profile.setTargetRole(dto.getTargetRole());
        profile.setResumeUrl(dto.getResumeUrl());
        profile.setPhoneNumber(dto.getPhoneNumber());
        profile.setBio(dto.getBio());
 
        UserProfile saved = userProfileRepository.save(profile);
 
        return mapToDto(user, saved);
    }
 
    private UserProfileDto mapToDto(User user, UserProfile profile) {
        UserProfileDto.UserProfileDtoBuilder builder = UserProfileDto.builder()
                .fullName(user.getFullName())
                .email(user.getEmail());
 
        if (profile != null) {
            builder.id(profile.getId())
                    .college(profile.getCollege())
                    .branch(profile.getBranch())
                    .graduationYear(profile.getGraduationYear())
                    .skills(profile.getSkills())
                    .targetRole(profile.getTargetRole())
                    .resumeUrl(profile.getResumeUrl())
                    .phoneNumber(profile.getPhoneNumber())
                    .bio(profile.getBio());
        }
 
        return builder.build();
    }
}
 
