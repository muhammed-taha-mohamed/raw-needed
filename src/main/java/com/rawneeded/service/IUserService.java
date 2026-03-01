package com.rawneeded.service;

import com.rawneeded.dto.auth.ForgetPasswordDTO;
import com.rawneeded.dto.auth.ForgotPasswordRequestDto;
import com.rawneeded.dto.auth.LoginDTO;
import com.rawneeded.dto.auth.LoginResponseDTO;
import com.rawneeded.dto.auth.ChangePasswordDTO;
import com.rawneeded.dto.admin.CreateAdminDto;
import com.rawneeded.dto.admin.UpdateAdminDto;
import com.rawneeded.dto.staff.CreateStaffDto;
import com.rawneeded.dto.user.CreateUserDto;
import com.rawneeded.dto.user.SearchOperationsSummaryDto;
import com.rawneeded.dto.user.SupplierInfo;
import com.rawneeded.dto.user.UserRequestDto;
import com.rawneeded.dto.user.UserResponseDto;
import com.rawneeded.dto.admin.AdminUserDetailsDto;
import com.rawneeded.enumeration.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IUserService {


    /* =========== AUTH =========== */
    UserResponseDto register(CreateUserDto dto);
    LoginResponseDTO login(LoginDTO loginDTO);
    void logout();
    void sendResetPasswordOTP(ForgotPasswordRequestDto requestDto);
    Boolean updatePasswordByOTP(ForgetPasswordDTO dto);
    Boolean changePassword(ChangePasswordDTO dto);



    /* =========== USER MANAGEMENT =========== */
    UserResponseDto createStaffUser(CreateStaffDto dto);
    Page<UserResponseDto> filterByOwnerId(String ownerId, Pageable pageable);
    UserResponseDto update(String id, UserRequestDto dto);
    void delete(String id);
    UserResponseDto findById(String id);


    Page<SupplierInfo> getAllSuppliers(Pageable pageable, String category);

    List<SupplierInfo> getAllSuppliers(String category);

    List<SearchOperationsSummaryDto> getSearchOperationsSummary(Integer year, Integer month);

    /* =========== ADMIN USER MANAGEMENT =========== */
    Page<UserResponseDto> getAllUsers(Pageable pageable);
    UserResponseDto activateUser(String userId);
    UserResponseDto deactivateUser(String userId);
    UserResponseDto createAdminUser(CreateAdminDto dto);
    UserResponseDto updateAdminUser(String userId, UpdateAdminDto dto);
    void deleteAdminUser(String userId);
    Page<UserResponseDto> getAdminUsers(Pageable pageable);

    Page<UserResponseDto> getSuppliers(Pageable pageable);

    Page<UserResponseDto> getCustomers(Pageable pageable);

    AdminUserDetailsDto getUserDetailsForAdmin(String userId);
}
