package com.rawneeded.service.impl;

import com.rawneeded.dto.profile.UpdateProfileDto;
import com.rawneeded.dto.user.UserResponseDto;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.mapper.UserMapper;
import com.rawneeded.model.User;
import com.rawneeded.repository.UserRepository;
import com.rawneeded.service.IProfileService;
import com.rawneeded.util.MessagesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class ProfileServiceImpl implements IProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final MessagesUtil messagesUtil;
    private static final String UPLOAD_DIR = "uploads/profiles/";

    @Override
    public UserResponseDto updateProfile(String userId, UpdateProfileDto dto) {
        try {
            log.info("Updating profile for user: {}", userId);
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_NOT_FOUND")));

            // Update fullName
            if (dto.getFullName() != null && !dto.getFullName().trim().isEmpty()) {
                user.setFullName(dto.getFullName());
            }

            // Update profile image
            if (dto.getProfileImage() != null && !dto.getProfileImage().isEmpty()) {
                String imagePath = saveProfileImage(dto.getProfileImage());
                user.setProfileImage(imagePath);
            }

            // Update password
            if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(dto.getPassword()));
            }

            // Update language preference
            if (dto.getLanguagePreference() != null) {
                user.setLanguagePreference(dto.getLanguagePreference());
            }

            user = userRepository.save(user);

            log.info("Profile updated successfully for user: {}", userId);
            return userMapper.toResponseDto(user);
        } catch (Exception e) {
            log.error("Error updating profile: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("PROFILE_UPDATE_FAIL"));
        }
    }

    @Override
    public UserResponseDto getProfile(String userId) {
        try {
            log.info("Fetching profile for user: {}", userId);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_NOT_FOUND")));
            return userMapper.toResponseDto(user);
        } catch (Exception e) {
            log.error("Error fetching profile: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("PROFILE_FETCH_FAIL"));
        }
    }

    private String saveProfileImage(MultipartFile file) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }
}
