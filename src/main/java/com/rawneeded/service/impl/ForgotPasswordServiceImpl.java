package com.rawneeded.service.impl;

import com.rawneeded.dto.MailDto;
import com.rawneeded.dto.auth.ForgotPasswordRequestDto;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.model.User;
import com.rawneeded.repository.UserRepository;
import com.rawneeded.service.IForgotPasswordService;
import com.rawneeded.util.MessagesUtil;
import com.rawneeded.util.OtpUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.rawneeded.enumeration.TemplateName.FORGET_PASSWORD_OTP;

@Slf4j
@Service
@AllArgsConstructor
public class ForgotPasswordServiceImpl implements IForgotPasswordService {

    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final MessagesUtil messagesUtil;

    @Override
    public void sendOTP(ForgotPasswordRequestDto requestDto) {
        try {
            log.info("Sending OTP to email: {}", requestDto.getEmail());
            
            User user = userRepository.findByEmailIgnoreCase(requestDto.getEmail())
                    .orElseThrow(() -> new AbstractException("User not found with email: " + requestDto.getEmail()));

            // Generate 6-digit OTP
            String otp = OtpUtil.generateOTP();

            // Save OTP in user entity (database)
            user.setForgetPasswordOTP(otp);
            userRepository.save(user);

            // Send OTP via email
            String subject = messagesUtil.getMessage("forgot.password.email.subject");
            new Thread(() -> {
                notificationService.sendEmail(
                        MailDto.builder()
                                .toEmail(requestDto.getEmail())
                                .subject(subject)
                                .templateName(FORGET_PASSWORD_OTP)
                                .build()
                );
            }).start();

            log.info("OTP sent successfully to email: {}", requestDto.getEmail());
        } catch (Exception e) {
            log.error("Error sending OTP: {}", e.getMessage());
            throw new AbstractException("Failed to send OTP: " + e.getMessage());
        }
    }

    @Override
    public Boolean verifyOTP(String email, String otp) {
        try {
            log.info("Verifying OTP for email: {}", email);
            
            // Check database
            User user = userRepository.findByEmailAndForgetPasswordOTP(email, otp)
                    .orElse(null);
            
            if (user != null) {
                // Clear OTP from user entity
                user.setForgetPasswordOTP(null);
                userRepository.save(user);
                log.info("OTP verified successfully for email: {}", email);
                return true;
            }

            log.warn("Invalid OTP for email: {}", email);
            return false;
        } catch (Exception e) {
            log.error("Error verifying OTP: {}", e.getMessage());
            throw new AbstractException("Failed to verify OTP: " + e.getMessage());
        }
    }
}
