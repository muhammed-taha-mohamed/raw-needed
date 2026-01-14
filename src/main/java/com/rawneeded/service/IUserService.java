package com.rawneeded.service;

import com.rawneeded.dto.auth.ForgetPasswordDTO;
import com.rawneeded.dto.auth.ForgotPasswordRequestDto;
import com.rawneeded.dto.auth.LoginDTO;
import com.rawneeded.dto.auth.LoginResponseDTO;
import com.rawneeded.dto.staff.CreateStaffDto;
import com.rawneeded.dto.user.CreateUserDto;
import com.rawneeded.dto.user.SupplierInfo;
import com.rawneeded.dto.user.UserRequestDto;
import com.rawneeded.dto.user.UserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IUserService {


    /* =========== AUTH =========== */
    UserResponseDto register(CreateUserDto dto);
    LoginResponseDTO login(LoginDTO loginDTO);
    void sendResetPasswordOTP(ForgotPasswordRequestDto requestDto);
    Boolean updatePasswordByOTP(ForgetPasswordDTO dto);



    /* =========== USER MANAGEMENT =========== */
    UserResponseDto createStaffUser(CreateStaffDto dto);
    Page<UserResponseDto> filterByOwnerId(String ownerId, Pageable pageable);
    UserResponseDto update(String id, UserRequestDto dto);
    void delete(String id);
    UserResponseDto findById(String id);


    Page<SupplierInfo> getAllSuppliers(Pageable pageable, String category);

    List<SupplierInfo> getAllSuppliers(String category);
}
