package com.rawneeded.service.impl;

import com.rawneeded.dto.MailDto;
import com.rawneeded.dto.auth.*;
import com.rawneeded.dto.user.CreateUserDto;
import com.rawneeded.dto.user.UserRequestDto;
import com.rawneeded.dto.user.UserResponseDto;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.jwt.JwtTokenProvider;
import com.rawneeded.mapper.UserMapper;
import com.rawneeded.model.User;
import com.rawneeded.repository.UserRepository;
import com.rawneeded.service.ICartService;
import com.rawneeded.service.IUserService;
import com.rawneeded.util.MessagesUtil;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.rawneeded.enummeration.TemplateName.*;


@Service
@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final MessagesUtil messagesUtil;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final UserMapper userMapper;
    private final JwtTokenProvider tokenProvider;
    private final ICartService cartService;


    @Override
    public UserResponseDto register(CreateUserDto dto) {
        try {
            log.info("Start to create a user by parameter: {}", dto);
            User user = userMapper.toEntity(dto);
            user.setPassword(user.getPassword() != null ? passwordEncoder.encode(user.getPassword()) : null);
            user = userRepository.save(user);

            String subject = messagesUtil.getMessage("welcome.email.subject");
            new Thread(() -> {
                notificationService.sendEmail(
                        MailDto.builder()
                                .toEmail(dto.getEmail())
                                .subject(subject)
                                .templateName(WELCOME_TEMPLATE)
                                .build()
                );
            }).start();

            cartService.create(user.getId());

            return userMapper.toResponseDto(user);
        } catch (DuplicateKeyException e) {
            throw new AbstractException(messagesUtil.getMessage("email.already.exists"));
        } catch (Exception e) {
            log.error("Failed to create a user due to : {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }

    @Override
    public String update(String id, UserRequestDto dto) {
        try {
            log.info("Start creating a user...");
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("not.found")));

            userMapper.update(user, dto);
            user = userRepository.save(user);
            return user.getId();
        } catch (DuplicateKeyException e) {
            throw new AbstractException(messagesUtil.getMessage("email.already.exists"));
        } catch (Exception e) {
            log.error("Failed to create a user due to : {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }


    @Override
    public void delete(String id) {
        try {
            log.error("Start to delete a user : {}", id);
            userRepository.deleteById(id);
        } catch (Exception e) {
            log.error("Failed to delete a user due to : {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }


    private Optional<User> loadUserByEmail(String email) {
        try {
            log.error("Start to get a user by email : {}", email);
            return userRepository.findByEmailIgnoreCase(email);
        } catch (Exception e) {
            log.error("Failed to get a user by email : {}", email);
            throw new AbstractException(e.getMessage());
        }
    }


    @Override
    public LoginResponseDTO login(LoginDTO loginDTO) {
        try {
            log.info("Start to login a user by email : {}", loginDTO.getEmail());
            Optional<User> userOptional = loadUserByEmail(loginDTO.getEmail());
            if (userOptional.isPresent()) {
                User user = userOptional.get();

                if (passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
                    String token = tokenProvider.generateToken(
                            GenerateTokenDto.builder()
                                    .id(user.getId())
                                    .name(user.getName())
                                    .email(user.getEmail())
                                    .role(user.getRole())
                                    .build());

                    log.info("user login successful.");
                    return LoginResponseDTO.builder()
                            .userId(user.getId())
                            .name(user.getName())
                            .email(user.getEmail())
                            .token(token)
                            .role(user.getRole())
                            .build();
                }
                throw new IllegalArgumentException(messagesUtil.getMessage("email.password.not.valid"));

            }
            throw new IllegalArgumentException(messagesUtil.getMessage("email.password.not.valid"));
        } catch (Exception e) {
            log.error("Error occurred during client login: {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }


    @Override
    public Boolean updatePasswordByOTP(ForgetPasswordDTO dto) {
        try {
            log.info("Updating password by OTP for email: {}", dto.getEmail());

            Optional<User> userOptional = userRepository.findByEmailAndForgetPasswordOTP(dto.getEmail(), dto.getOtp());

            // If the user is found, update the password and save the user
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setForgetPasswordOTP(null);
                user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
                userRepository.save(user);

                log.info("Password updated successfully for user: {}", user.getId());
                return true;
            }

            log.error("Failed to update password by OTP for email: {}. OTP not valid.", dto.getEmail());
            throw new IllegalArgumentException(messagesUtil.getMessage("otp.not.valid"));
        } catch (Exception e) {
            log.error("Error occurred while updating password by OTP for email {}: {}", dto.getEmail(), e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }

}
