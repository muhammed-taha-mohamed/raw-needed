package com.rawneeded.service;

import com.rawneeded.dto.staff.CreateStaffDto;
import com.rawneeded.dto.user.UserResponseDto;

public interface IStaffService {
    UserResponseDto createStaff(CreateStaffDto dto);
}
