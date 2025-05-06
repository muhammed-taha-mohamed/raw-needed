package com.rawneeded.service;

import com.rawneeded.dto.auth.ForgetPasswordDTO;
import com.rawneeded.dto.auth.LoginDTO;
import com.rawneeded.dto.auth.LoginResponseDTO;
import com.rawneeded.dto.user.CreateUserDto;
import com.rawneeded.dto.user.UserRequestDto;
import com.rawneeded.dto.user.UserResponseDto;

public interface IQuotationService {

    void sendQuotationRequests(String userId);
}
