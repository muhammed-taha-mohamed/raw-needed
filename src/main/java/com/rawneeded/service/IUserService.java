package com.rawneeded.service;

import com.rawneeded.dto.auth.ForgetPasswordDTO;
import com.rawneeded.dto.auth.ForgotPasswordRequestDto;
import com.rawneeded.dto.auth.LoginDTO;
import com.rawneeded.dto.auth.LoginResponseDTO;
import com.rawneeded.dto.staff.CreateStaffDto;
import com.rawneeded.dto.user.CreateUserDto;
import com.rawneeded.dto.user.UserRequestDto;
import com.rawneeded.dto.user.UserResponseDto;

public interface IUserService {


    UserResponseDto register(CreateUserDto dto);

    UserResponseDto createStafUser(CreateStaffDto dto);

    UserResponseDto update(String id, UserRequestDto dto);

    void delete(String id);

    LoginResponseDTO login(LoginDTO loginDTO);

    UserResponseDto findById(String id);


    void sendResetPasswordOTP(ForgotPasswordRequestDto requestDto);

    Boolean updatePasswordByOTP(ForgetPasswordDTO dto);
}
