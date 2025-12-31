package com.rawneeded.service;

import com.rawneeded.dto.profile.UpdateProfileDto;
import com.rawneeded.dto.user.UserResponseDto;

public interface IProfileService {
    UserResponseDto updateProfile(String userId, UpdateProfileDto dto);
    UserResponseDto getProfile(String userId);
}
