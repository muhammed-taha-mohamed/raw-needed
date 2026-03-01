package com.rawneeded.service.impl;

import com.rawneeded.dto.MailDto;
import com.rawneeded.dto.auth.*;
import com.rawneeded.dto.admin.CreateAdminDto;
import com.rawneeded.dto.admin.UpdateAdminDto;
import com.rawneeded.dto.staff.CreateStaffDto;
import com.rawneeded.dto.user.CreateUserDto;
import com.rawneeded.dto.user.SearchOperationsSummaryDto;
import com.rawneeded.dto.user.SupplierInfo;
import com.rawneeded.dto.admin.AdminUserDetailsDto;
import com.rawneeded.dto.admin.CustomerStatsDto;
import com.rawneeded.dto.admin.SupplierStatsDto;
import com.rawneeded.dto.user.UserRequestDto;
import com.rawneeded.dto.user.UserResponseDto;
import com.rawneeded.enumeration.AccountStatus;
import com.rawneeded.enumeration.LineStatus;
import com.rawneeded.enumeration.OrderStatus;
import com.rawneeded.enumeration.Role;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.error.exceptions.ExistingSessionException;
import com.rawneeded.jwt.JwtTokenProvider;
import com.rawneeded.mapper.UserMapper;
import com.rawneeded.model.Category;
import com.rawneeded.model.SubCategory;
import com.rawneeded.model.RFQOrderLine;
import com.rawneeded.model.User;
import com.rawneeded.model.UserSession;
import com.rawneeded.model.UserSubscription;
import com.rawneeded.repository.CategoryRepository;
import com.rawneeded.repository.UserSessionRepository;
import com.rawneeded.repository.SubCategoryRepository;
import com.rawneeded.repository.ProductRepository;
import com.rawneeded.repository.RFQOrderLineRepository;
import com.rawneeded.repository.RFQOrderRepository;
import com.rawneeded.repository.UserRepository;
import com.rawneeded.service.ICartService;
import com.rawneeded.service.IUserService;
import com.rawneeded.service.IUserSubscriptionService;
import com.rawneeded.util.MessagesUtil;
import com.rawneeded.util.OtpUtil;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.UUID;

import static com.rawneeded.enumeration.TemplateName.FORGET_PASSWORD_OTP;
import static com.rawneeded.enumeration.TemplateName.WELCOME_TEMPLATE;

@Service
@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final MongoTemplate mongoTemplate;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final JwtTokenProvider tokenProvider;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final ICartService cartService;
    private final MessagesUtil messagesUtil;
    private final IUserSubscriptionService subscriptionService;
    private final RFQOrderRepository orderRepository;
    private final RFQOrderLineRepository orderLineRepository;
    private final ProductRepository productRepository;

    // ================= AUTH METHODS ================= //

    @Override
    public UserResponseDto register(CreateUserDto dto) {
        try {
            log.info("Start creating a user with params: {}", dto);
            if (dto.getPassword() == null || !dto.getPassword().equals(dto.getConfirmPassword())) {
                throw new AbstractException(messagesUtil.getMessage("PASSWORDS_MISMATCH"));
            }

            User user = userMapper.toEntity(dto);
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setAccountStatus(AccountStatus.ACTIVE);

            // ===== Preferred Category & SubCategories =====
            if (dto.getCategoryId() != null && !dto.getCategoryId().isEmpty()) {
                Category category = categoryRepository.findById(dto.getCategoryId())
                        .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("CATEGORY_NOT_FOUND")));
                user.setCategory(category);

                if (dto.getSubCategoryIds() != null && !dto.getSubCategoryIds().isEmpty()) {
                    List<SubCategory> categorySubCategories = subCategoryRepository.findByCategoryId(category.getId());

                    List<SubCategory> selectedSubCategories = categorySubCategories.stream()
                            .filter(sub -> dto.getSubCategoryIds().contains(sub.getId()))
                            .toList();

                    if (selectedSubCategories.size() != dto.getSubCategoryIds().size()) {
                        throw new AbstractException(messagesUtil.getMessage("SUBCATEGORY_SELECTED_NOT_FOUND"));
                    }

                    user.setSubCategories(selectedSubCategories);
                }
            }


            // ===== Create User =====
            user = userRepository.save(user);


            // Send welcome email asynchronously
            new Thread(() -> {
                notificationService.sendEmail(
                        MailDto.builder()
                                .toEmail(dto.getEmail())
                                .subject(messagesUtil.getMessage("WELCOME_EMAIL_SUBJECT"))
                                .templateName(WELCOME_TEMPLATE)
                                .build()
                );
            }).start();

            cartService.create(user.getId());

            return userMapper.toResponseDto(user);

        }catch (AbstractException e){
            throw e;
        } catch (DuplicateKeyException e) {
            throw new AbstractException(messagesUtil.getMessage("USER_ALREADY_EXISTS"));
        } catch (Exception e) {
            log.error("Failed to create user: {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }

    @Override
    public LoginResponseDTO login(LoginDTO dto) {
        User user = userRepository.findByEmailIgnoreCase(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException(messagesUtil.getMessage("EMAIL_PASSWORD_NOT_VALID")));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException(messagesUtil.getMessage("EMAIL_PASSWORD_NOT_VALID"));
        }

        // Check if account is active (skip check for SUPER_ADMIN)
        if (user.getAccountStatus() == AccountStatus.INACTIVE && user.getRole() != Role.SUPER_ADMIN) {
            throw new AbstractException(messagesUtil.getMessage("ACCOUNT_INACTIVE"));
        }

        // Single session per user: check existing session
        Optional<UserSession> existingSession = userSessionRepository.findByUserId(user.getId());
        boolean forceReplace = Boolean.TRUE.equals(dto.getForceReplaceSession());

        if (existingSession.isPresent() && !forceReplace) {
            throw new ExistingSessionException(messagesUtil.getMessage("EXISTING_SESSION"));
        }

        String newSessionId = UUID.randomUUID().toString();
        UserSession userSession = existingSession
                .map(s -> {
                    s.setCurrentSessionId(newSessionId);
                    return s;
                })
                .orElse(UserSession.builder()
                        .userId(user.getId())
                        .currentSessionId(newSessionId)
                        .build());
        userSessionRepository.save(userSession);

        String token = tokenProvider.generateToken(
                GenerateTokenDto.builder()
                        .id(user.getId())
                        .ownerId(user.getOwnerId()!=null?user.getOwnerId():user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .phoneNumber(user.getPhoneNumber())
                        .sessionId(newSessionId)
                        .build());

        UserResponseDto userInfo = userMapper.toResponseDto(user);

        return LoginResponseDTO.builder()
                .token(token)
                .userInfo(userInfo)
                .build();
    }

    @Override
    public void logout() {
        String token = messagesUtil.getAuthToken();
        if (token == null || token.isBlank()) return;
        try {
            String userId = tokenProvider.getIdFromToken(token);
            if (userId != null) userSessionRepository.deleteByUserId(userId);
        } catch (Exception e) {
            log.debug("Logout: could not invalidate session (e.g. expired token): {}", e.getMessage());
        }
    }

    @Override
    public void sendResetPasswordOTP(ForgotPasswordRequestDto requestDto) {
        try {
            log.info("Sending OTP to email: {}", requestDto.getEmail());

            User user = userRepository.findByEmailIgnoreCase(requestDto.getEmail())
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_NOT_FOUND_EMAIL")));

            // Generate 6-digit OTP
            String otp = OtpUtil.generateOTP();

            // Save OTP in user entity (database)
            user.setForgetPasswordOTP(otp);
            userRepository.save(user);

            // Send OTP via email
            String subject = messagesUtil.getMessage("FORGOT_PASSWORD_EMAIL_SUBJECT");
            new Thread(() -> {
                notificationService.sendEmail(
                        MailDto.builder()
                                .toEmail(requestDto.getEmail())
                                .subject(subject)
                                .model(Map.of("code", otp))
                                .templateName(FORGET_PASSWORD_OTP)
                                .build()
                );
            }).start();

            log.info("OTP sent successfully to email: {}", requestDto.getEmail());
        }catch (AbstractException e){
            throw e;
        }catch (Exception e) {
            log.error("Error sending OTP: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("OTP_SEND_FAIL"));
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
            throw new IllegalArgumentException(messagesUtil.getMessage("OTP_NOT_VALID"));
        }catch (AbstractException e){
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while updating password by OTP for email {}: {}", dto.getEmail(), e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }

    @Override
    public Boolean changePassword(ChangePasswordDTO dto) {
        try {
            String token = messagesUtil.getAuthToken();
            String userId = tokenProvider.getIdFromToken(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_NOT_FOUND")));

            if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
                throw new AbstractException(messagesUtil.getMessage("OLD_PASSWORD_INCORRECT"));
            }
            if (dto.getNewPassword() == null || !dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
                throw new AbstractException(messagesUtil.getMessage("PASSWORDS_MISMATCH"));
            }

            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
            userRepository.save(user);
            return true;
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to change password: {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }


    /* =========== USER MANAGEMENT =========== */

    @Override
    public UserResponseDto createStaffUser(CreateStaffDto dto) {
        try {
            log.info("Creating staff member: {}", dto.getEmail());


            String token = messagesUtil.getAuthToken();
            String ownerId = tokenProvider.getIdFromToken(token);

            // Validate owner exists
            User owner = userRepository.findById(ownerId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("OWNER_NOT_FOUND")));

            UserSubscription ownerSubscription = owner.getSubscription();
            subscriptionService.updateUsedUsers(ownerSubscription.getId(), true);

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
                    .subscription(ownerSubscription)
                    .organizationName(owner.getOrganizationName())
                    .organizationCRN(owner.getOrganizationCRN())
                    .organizationCRNImage(owner.getOrganizationCRNImage())
                    .build();

            staff = userRepository.save(staff);

            log.info("Staff member created successfully: {}", staff.getId());
            return userMapper.toResponseDto(staff);
        }catch (AbstractException e){
            throw e;
        } catch ( Exception e) {
            throw new AbstractException(e.getMessage());
        }
    }

    @Override
    public Page<UserResponseDto> filterByOwnerId(String ownerId, Pageable pageable) {
        try {
            return userRepository.findAllByOwnerId(ownerId, pageable)
                    .map(userMapper::toResponseDto);
        } catch (Exception e) {
            log.error("Failed to get users: {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }

    @Override
    public UserResponseDto update(String userId, UserRequestDto dto) {
        try {
            log.info("Start updating a user: {}", userId);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("NOT_FOUND")));

            userMapper.update(user, dto);

            // Update preferred category & subcategories
            if (dto.getCategoryId() != null) {
                Category category = categoryRepository.findById(dto.getCategoryId())
                        .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("CATEGORY_NOT_FOUND")));
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

        }catch (AbstractException e){
            throw e;
        } catch (DuplicateKeyException e) {
            throw new AbstractException(messagesUtil.getMessage("USER_ALREADY_EXISTS"));
        } catch (Exception e) {
            log.error("Failed to update user: {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }

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
    public UserResponseDto findById(String userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("NOT_FOUND")));
            return userMapper.toResponseDto(user);
        } catch (Exception e) {
            log.error("Failed to get user: {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }

    @Override
    public Page<SupplierInfo> getAllSuppliers(Pageable pageable, String category) {
        try {
            log.info("Fetching all suppliers");
            if (category != null) {
                return userMapper.toSupplierResponsePages(
                        userRepository.findAllByRoleAndCategory_Id(Role.SUPPLIER_OWNER, category, pageable));
            }
            return userMapper.toSupplierResponsePages(
                    userRepository.findAllByRole(Role.SUPPLIER_OWNER, pageable)
            );
        }catch (AbstractException e){
            throw e;
        } catch (Exception e) {
            log.error("Failed to get suppliers: {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }

    @Override
    public List<SupplierInfo> getAllSuppliers(String category) {
        try {
            log.info("Fetching all suppliers");
            if (category != null) {
                return userMapper.toSupplierResponseList(
                        userRepository.findAllByRoleAndCategory_Id(Role.SUPPLIER_OWNER, category));
            }
            return userMapper.toSupplierResponseList(
                    userRepository.findAllByRole(Role.SUPPLIER_OWNER)
            );
        }catch (AbstractException e){
            throw e;
        } catch (Exception e) {
            log.error("Failed to get suppliers: {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }

    @Override
    public List<SearchOperationsSummaryDto> getSearchOperationsSummary(Integer year, Integer month) {
        try {
            String token = messagesUtil.getAuthToken();
            String ownerId = tokenProvider.getOwnerIdFromToken(token);

            YearMonth ym = (year != null && month != null)
                    ? YearMonth.of(year, month)
                    : YearMonth.now();
            LocalDateTime start = ym.atDay(1).atStartOfDay();
            LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay();

            Aggregation aggregation = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("ownerId").is(ownerId)
                            .and("searchedAt").gte(start).lt(end)),
                    Aggregation.group("userId")
                            .first("userName").as("userName")
                            .count().as("searchCount"),
                    Aggregation.project("userName", "searchCount")
                            .and("_id").as("userId"),
                    Aggregation.sort(org.springframework.data.domain.Sort.Direction.DESC, "searchCount")
            );

            AggregationResults<SearchOperationsSummaryDto> aggResult =
                    mongoTemplate.aggregate(aggregation, "searchActivity", SearchOperationsSummaryDto.class);

            Map<String, SearchOperationsSummaryDto> summaryMap = new HashMap<>();
            for (SearchOperationsSummaryDto dto : aggResult.getMappedResults()) {
                summaryMap.put(dto.getUserId(), dto);
            }

            // Include owner + staff even with zero searches
            List<User> users = userRepository.findAllByOwnerId(ownerId);
            userRepository.findById(ownerId).ifPresent(users::add);

            for (User user : users) {
                summaryMap.putIfAbsent(
                        user.getId(),
                        SearchOperationsSummaryDto.builder()
                                .userId(user.getId())
                                .userName(user.getName())
                                .searchCount(0L)
                                .build()
                );
            }

            return summaryMap.values().stream()
                    .sorted((a, b) -> {
                        int byCount = Long.compare(b.getSearchCount() == null ? 0L : b.getSearchCount(),
                                a.getSearchCount() == null ? 0L : a.getSearchCount());
                        if (byCount != 0) return byCount;
                        String an = a.getUserName() == null ? "" : a.getUserName();
                        String bn = b.getUserName() == null ? "" : b.getUserName();
                        return an.compareToIgnoreCase(bn);
                    })
                    .toList();
        } catch (Exception e) {
            log.error("Failed to fetch search operations summary: {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }
    @Override
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        try {
            log.info("Fetching all users");
            Page<User> users = userRepository.findAll(pageable);
            return userMapper.toResponsePages(users);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get all users: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("FAILED_TO_FETCH_USERS"));
        }
    }

    @Override
    public Page<UserResponseDto> getSuppliers(Pageable pageable) {
        try {
            Page<User> users = userRepository.findAllByRole(Role.SUPPLIER_OWNER, pageable);
            return userMapper.toResponsePages(users);
        } catch (Exception e) {
            log.error("Failed to get suppliers: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("FAILED_TO_FETCH_USERS"));
        }
    }

    @Override
    public Page<UserResponseDto> getCustomers(Pageable pageable) {
        try {
            Page<User> users = userRepository.findAllByRole(Role.CUSTOMER_OWNER, pageable);
            return userMapper.toResponsePages(users);
        } catch (Exception e) {
            log.error("Failed to get customers: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("FAILED_TO_FETCH_USERS"));
        }
    }

    @Override
    public AdminUserDetailsDto getUserDetailsForAdmin(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_NOT_FOUND")));
        UserResponseDto userDto = userMapper.toResponseDto(user);
        List<UserResponseDto> staff = List.of();
        if (user.getRole() == Role.SUPPLIER_OWNER || user.getRole() == Role.CUSTOMER_OWNER) {
            List<User> staffList = userRepository.findAllByOwnerId(user.getId());
            staff = staffList.stream().map(userMapper::toResponseDto).toList();
        }
        SupplierStatsDto supplierStats = null;
        CustomerStatsDto customerStats = null;
        if (user.getRole() == Role.SUPPLIER_OWNER) {
            long totalProducts = productRepository.countBySupplier_Id(userId);
            long inStockProducts = productRepository.countBySupplier_IdAndInStockTrue(userId);
            long totalOrderLines = orderLineRepository.countBySupplierId(userId);
            List<RFQOrderLine> lines = orderLineRepository.findBySupplierId(userId);
            long distinctOrders = lines.stream().map(RFQOrderLine::getOrderId).distinct().count();
            Map<String, Long> orderLinesByStatus = new java.util.HashMap<>();
            for (LineStatus status : LineStatus.values()) {
                long c = orderLineRepository.countBySupplierIdAndStatus(userId, status);
                if (c > 0) orderLinesByStatus.put(status.name(), c);
            }
            supplierStats = SupplierStatsDto.builder()
                    .totalProducts(totalProducts)
                    .inStockProducts(inStockProducts)
                    .totalOrderLines(totalOrderLines)
                    .distinctOrders(distinctOrders)
                    .orderLinesByStatus(orderLinesByStatus)
                    .build();
        }
        if (user.getRole() == Role.CUSTOMER_OWNER) {
            long totalOrders = orderRepository.countByOwnerId(userId);
            Map<String, Long> ordersByStatus = new java.util.HashMap<>();
            for (OrderStatus status : OrderStatus.values()) {
                long c = orderRepository.countByOwnerIdAndStatus(userId, status);
                if (c > 0) ordersByStatus.put(status.name(), c);
            }
            customerStats = CustomerStatsDto.builder()
                    .totalOrders(totalOrders)
                    .ordersByStatus(ordersByStatus)
                    .build();
        }
        return AdminUserDetailsDto.builder()
                .user(userDto)
                .staff(staff)
                .supplierStats(supplierStats)
                .customerStats(customerStats)
                .build();
    }

    @Override
    public UserResponseDto activateUser(String userId) {
        try {
            log.info("Activating user: {}", userId);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_NOT_FOUND")));

            user.setAccountStatus(AccountStatus.ACTIVE);
            user = userRepository.save(user);

            // If user is OWNER, activate all their staff members
            if (user.getRole() == Role.CUSTOMER_OWNER || user.getRole() == Role.SUPPLIER_OWNER) {
                List<User> staffMembers = userRepository.findAllByOwnerId(user.getId());
                for (User staff : staffMembers) {
                    staff.setAccountStatus(AccountStatus.ACTIVE);
                    userRepository.save(staff);
                    log.info("Activated staff member: {}", staff.getId());
                }
            }

            return userMapper.toResponseDto(user);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to activate user: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("FAILED_TO_ACTIVATE_USER"));
        }
    }

    @Override
    public UserResponseDto deactivateUser(String userId) {
        try {
            log.info("Deactivating user: {}", userId);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_NOT_FOUND")));

            if (user.getRole() == Role.SUPER_ADMIN) {
                throw new AbstractException(messagesUtil.getMessage("UNAUTHORIZED"));
            }

            user.setAccountStatus(AccountStatus.INACTIVE);
            user = userRepository.save(user);

            // If user is OWNER, deactivate all their staff members
            if (user.getRole() == Role.CUSTOMER_OWNER || user.getRole() == Role.SUPPLIER_OWNER) {
                List<User> staffMembers = userRepository.findAllByOwnerId(user.getId());
                for (User staff : staffMembers) {
                    staff.setAccountStatus(AccountStatus.INACTIVE);
                    userRepository.save(staff);
                    log.info("Deactivated staff member: {}", staff.getId());
                }
            }

            return userMapper.toResponseDto(user);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to deactivate user: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("FAILED_TO_DEACTIVATE_USER"));
        }
    }

    @Override
    public UserResponseDto createAdminUser(CreateAdminDto dto) {
        try {
            assertSuperAdminAccess();
            Role requestedRole = dto.getRole();
            if (requestedRole != Role.ADMIN && requestedRole != Role.SUPER_ADMIN) {
                throw new AbstractException(messagesUtil.getMessage("UNAUTHORIZED"));
            }
            if (dto.getPassword() == null || !dto.getPassword().equals(dto.getConfirmPassword())) {
                throw new AbstractException(messagesUtil.getMessage("PASSWORDS_MISMATCH"));
            }

            User adminUser = User.builder()
                    .name(dto.getName())
                    .email(dto.getEmail())
                    .phoneNumber(dto.getPhoneNumber())
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .role(requestedRole)
                    .accountStatus(AccountStatus.ACTIVE)
                    .build();
            adminUser = userRepository.save(adminUser);
            return userMapper.toResponseDto(adminUser);
        } catch (AbstractException e) {
            throw e;
        } catch (DuplicateKeyException e) {
            throw new AbstractException(messagesUtil.getMessage("USER_ALREADY_EXISTS"));
        } catch (Exception e) {
            log.error("Failed to create admin user: {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }

    @Override
    public Page<UserResponseDto> getAdminUsers(Pageable pageable) {
        try {
            assertSuperAdminAccess();
            return userRepository.findAllByRoleIn(List.of(Role.SUPER_ADMIN, Role.ADMIN), pageable)
                    .map(user -> {
                        if (user.getRole() == Role.SUPER_ADMIN && user.getAccountStatus() != AccountStatus.ACTIVE) {
                            user.setAccountStatus(AccountStatus.ACTIVE);
                            user = userRepository.save(user);
                        }
                        return userMapper.toResponseDto(user);
                    });
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch admin users: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("FAILED_TO_FETCH_USERS"));
        }
    }

    @Override
    public UserResponseDto updateAdminUser(String userId, UpdateAdminDto dto) {
        try {
            assertSuperAdminAccess();

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_NOT_FOUND")));

            if (user.getRole() != Role.ADMIN && user.getRole() != Role.SUPER_ADMIN) {
                throw new AbstractException(messagesUtil.getMessage("UNAUTHORIZED"));
            }

            Role requestedRole = dto.getRole();
            if (requestedRole != Role.ADMIN && requestedRole != Role.SUPER_ADMIN) {
                throw new AbstractException(messagesUtil.getMessage("UNAUTHORIZED"));
            }

            user.setName(dto.getName());
            user.setEmail(dto.getEmail());
            user.setPhoneNumber(dto.getPhoneNumber());
            user.setRole(requestedRole);

            if (user.getRole() == Role.SUPER_ADMIN) {
                user.setAccountStatus(AccountStatus.ACTIVE);
            }

            if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
                if (!dto.getPassword().equals(dto.getConfirmPassword())) {
                    throw new AbstractException(messagesUtil.getMessage("PASSWORDS_MISMATCH"));
                }
                user.setPassword(passwordEncoder.encode(dto.getPassword()));
            }

            user = userRepository.save(user);
            return userMapper.toResponseDto(user);
        } catch (AbstractException e) {
            throw e;
        } catch (DuplicateKeyException e) {
            throw new AbstractException(messagesUtil.getMessage("USER_ALREADY_EXISTS"));
        } catch (Exception e) {
            log.error("Failed to update admin user: {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }

    @Override
    public void deleteAdminUser(String userId) {
        try {
            assertSuperAdminAccess();
            String token = messagesUtil.getAuthToken();
            String currentUserId = tokenProvider.getIdFromToken(token);
            if (currentUserId != null && currentUserId.equals(userId)) {
                throw new AbstractException(messagesUtil.getMessage("UNAUTHORIZED"));
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_NOT_FOUND")));

            if (user.getRole() == Role.SUPER_ADMIN) {
                throw new AbstractException(messagesUtil.getMessage("UNAUTHORIZED"));
            }
            if (user.getRole() != Role.ADMIN) {
                throw new AbstractException(messagesUtil.getMessage("UNAUTHORIZED"));
            }

            userRepository.deleteById(userId);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete admin user: {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }

    private void assertSuperAdminAccess() {
        String token = messagesUtil.getAuthToken();
        Role role = tokenProvider.getRoleFromToken(token);
        if (role != Role.SUPER_ADMIN) {
            throw new AbstractException(messagesUtil.getMessage("UNAUTHORIZED"));
        }
    }

    @PostConstruct
    public void initSuperAdmin() {
        if (userRepository.existsByRole(Role.SUPER_ADMIN)) {
            log.info("Super Admin already exists, skipping initialization.");
            return;
        }

        User superAdmin = User.builder()
                .name("Raw needed super admin")
                .email("admin@rawneeded.com")
                .password(passwordEncoder.encode("admin@123"))
                .role(Role.SUPER_ADMIN)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        userRepository.save(superAdmin);
    }
}