package com.rawneeded.service.impl;

import com.rawneeded.dto.MailDto;
import com.rawneeded.dto.auth.*;
import com.rawneeded.dto.staff.CreateStaffDto;
import com.rawneeded.dto.user.CreateUserDto;
import com.rawneeded.dto.user.UserRequestDto;
import com.rawneeded.dto.user.UserResponseDto;
import com.rawneeded.enumeration.AccountStatus;
import com.rawneeded.enumeration.Role;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.error.exceptions.PlanLimitExceededException;
import com.rawneeded.jwt.JwtTokenProvider;
import com.rawneeded.mapper.UserMapper;
import com.rawneeded.model.Category;
import com.rawneeded.model.SubCategory;
import com.rawneeded.model.SubscriptionPlan;
import com.rawneeded.model.User;
import com.rawneeded.repository.CategoryRepository;
import com.rawneeded.repository.SubCategoryRepository;
import com.rawneeded.repository.UserRepository;
import com.rawneeded.service.ICartService;
import com.rawneeded.service.IUserService;
import com.rawneeded.util.MessagesUtil;
import com.rawneeded.util.OtpUtil;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.rawneeded.enumeration.TemplateName.FORGET_PASSWORD_OTP;
import static com.rawneeded.enumeration.TemplateName.WELCOME_TEMPLATE;

@Service
@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final JwtTokenProvider tokenProvider;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final ICartService cartService;
    private final MessagesUtil messagesUtil;

    // ================= Register User =================
    @Override
    public UserResponseDto register(CreateUserDto dto) {
        try {
            log.info("Start creating a user with params: {}", dto);

            User user = userMapper.toEntity(dto);
            user.setPassword(passwordEncoder.encode(dto.getPassword()));


            // ===== Preferred Category & SubCategories =====
            if (dto.getCategoryId() != null && !dto.getCategoryId().isEmpty()) {
                Category category = categoryRepository.findById(dto.getCategoryId())
                        .orElseThrow(() -> new AbstractException("Category not found"));
                user.setCategory(category);

                if (dto.getSubCategoryIds() != null && !dto.getSubCategoryIds().isEmpty()) {
                    List<SubCategory> categorySubCategories = subCategoryRepository.findByCategoryId(category.getId());

                    List<SubCategory> selectedSubCategories = categorySubCategories.stream()
                            .filter(sub -> dto.getSubCategoryIds().contains(sub.getId()))
                            .toList();

                    if (selectedSubCategories.size() != dto.getSubCategoryIds().size()) {
                        throw new AbstractException("Some subCategories not found in the selected category");
                    }

                    user.setSubCategories(selectedSubCategories);
                }
            }

            user = userRepository.save(user);

            // Send welcome email asynchronously
            new Thread(() -> {
                notificationService.sendEmail(
                        MailDto.builder()
                                .toEmail(dto.getEmail())
                                .subject(messagesUtil.getMessage("welcome.email.subject"))
                                .templateName(WELCOME_TEMPLATE)
                                .build()
                );
            }).start();

            cartService.create(user.getId());

            return userMapper.toResponseDto(user);

        } catch (DuplicateKeyException e) {
            throw new AbstractException(messagesUtil.getMessage("email.already.exists"));
        } catch (Exception e) {
            log.error("Failed to create user: {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }


    @Override
    public UserResponseDto createStafUser(CreateStaffDto dto) {
        try {
            log.info("Creating staff member: {}", dto.getEmail());


            String token = messagesUtil.getAuthToken();
            String ownerId = tokenProvider.getIdFromToken(token);

            // Validate owner exists
            User owner = userRepository.findById(ownerId)
                    .orElseThrow(() -> new AbstractException("Owner not found"));


            // Create staff user
            User staff = User.builder()
                    .name(dto.getName())
                    .email(dto.getEmail())
                    .phoneNumber(dto.getPhoneNumber())
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .role(owner.getRole().equals(Role.CUSTOMER_OWNER) ? Role.CUSTOMER_STAFF :
                            Role.SUPPLIER_STAFF)
                    .ownerId(ownerId)
                    .accountStatus(AccountStatus.ACTIVE)
                    .allowedScreens(dto.getAllowedScreenIds())
                    .category(owner.getCategory())
                    .subCategories(owner.getSubCategories())
                    .build();


            staff = userRepository.save(staff);

            log.info("Staff member created successfully: {}", staff.getId());
            return userMapper.toResponseDto(staff);
        } catch (PlanLimitExceededException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating staff member: {}", e.getMessage());
            throw new AbstractException("Failed to create staff member: " + e.getMessage());
        }
    }


    // ================= Update User =================
    @Override
    public UserResponseDto update(String userId, UserRequestDto dto) {
        try {
            log.info("Start updating a user: {}", userId);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("not.found")));

            userMapper.update(user, dto);

            // Update preferred category & subcategories
            if (dto.getCategoryId() != null) {
                Category category = categoryRepository.findById(dto.getCategoryId())
                        .orElseThrow(() -> new AbstractException("Category not found"));
                user.setCategory(category);
            }

            if (dto.getSubCategoryIds() != null && user.getCategory() != null) {
                List<SubCategory> categorySubCategories = subCategoryRepository.findByCategoryId(user.getCategory().getId());

                List<SubCategory> selectedSubCategories = categorySubCategories.stream()
                        .filter(sub -> dto.getSubCategoryIds().contains(sub.getId()))
                        .toList();

                user.setSubCategories(selectedSubCategories);
            }


            user = userRepository.save(user);
            return userMapper.toResponseDto(user);

        } catch (DuplicateKeyException e) {
            throw new AbstractException(messagesUtil.getMessage("email.already.exists"));
        } catch (Exception e) {
            log.error("Failed to update user: {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }

    // ================= Delete User =================
    @Override
    public void delete(String userId) {
        try {
            log.info("Deleting user: {}", userId);
            userRepository.deleteById(userId);
        } catch (Exception e) {
            log.error("Failed to delete user: {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }



    @Override
    public void sendResetPasswordOTP(ForgotPasswordRequestDto requestDto) {
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

    // ================= Find User =================
    @Override
    public UserResponseDto findById(String userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("not.found")));
            return userMapper.toResponseDto(user);
        } catch (Exception e) {
            log.error("Failed to get user: {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }

    // ================= Login =================
    @Override
    public LoginResponseDTO login(LoginDTO dto) {
        User user = userRepository.findByEmailIgnoreCase(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException(messagesUtil.getMessage("email.password.not.valid")));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException(messagesUtil.getMessage("email.password.not.valid"));
        }

        String token = tokenProvider.generateToken(
                GenerateTokenDto.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .build());

        return LoginResponseDTO.builder()
                .token(token)
                .userInfo(userMapper.toResponseDto(user))
                .build();
    }

    @PostConstruct
    public void initSuperAdmin() {
        if (userRepository.existsByRole(Role.SUPER_ADMIN)) {
            log.info("Super Admin already exists, skipping initialization.");
            return;
        }

        User superAdmin = User.builder()
                .name("Raw needed super admin")
                .fullName("Raw needed super admin")
                .email("admin@rawneeded.com")
                .password(passwordEncoder.encode("admin@123"))
                .role(Role.SUPER_ADMIN)
                .build();

        userRepository.save(superAdmin);
    }
}