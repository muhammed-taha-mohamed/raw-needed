package com.rawneeded.service.impl;

import com.rawneeded.dto.subscription.CalculatePriceRequestDto;
import com.rawneeded.dto.subscription.CalculatePriceResponseDto;
import com.rawneeded.dto.subscription.CreateSubscriptionRequestDto;
import com.rawneeded.dto.subscription.UserSubscriptionRequestDto;
import com.rawneeded.dto.subscription.UserSubscriptionResponseDto;
import com.rawneeded.enumeration.AccountStatus;
import com.rawneeded.enumeration.UserSubscriptionStatus;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.jwt.JwtTokenProvider;
import com.rawneeded.mapper.UserSubscriptionMapper;
import com.rawneeded.model.SpecialOffer;
import com.rawneeded.model.SubscriptionPlan;
import com.rawneeded.model.User;
import com.rawneeded.model.UserSubscription;
import com.rawneeded.repository.SubscriptionPlanRepository;
import com.rawneeded.repository.UserRepository;
import com.rawneeded.repository.UserSubscriptionRepository;
import com.rawneeded.service.IUserSubscriptionService;
import com.rawneeded.util.MessagesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.rawneeded.enumeration.UserSubscriptionStatus.PENDING;

@Slf4j
@Service
@AllArgsConstructor
public class UserSubscriptionServiceImpl implements IUserSubscriptionService {

    private static final String UPLOAD_DIR = "uploads/user-subscriptions/";
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserSubscriptionMapper subscriptionMapper;
    private final UserRepository userRepository;
    private final MessagesUtil messagesUtil;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public CalculatePriceResponseDto calculatePrice(CalculatePriceRequestDto requestDto) {
        try {
            log.info("Calculating price for plan: {} with {} users", requestDto.getPlanId(), requestDto.getNumberOfUsers());

            SubscriptionPlan plan = subscriptionPlanRepository.findById(requestDto.getPlanId())
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_SUB_PLAN_NOT_FOUND")));

            int numberOfUsers = requestDto.getNumberOfUsers();
            double pricePerUser = plan.getPricePerUser();
            double total = pricePerUser * numberOfUsers;

            // Calculate the discount and find the best offer
            double discount = calculateDiscount(plan, numberOfUsers);
            double finalPrice = total - discount;

            // Find the best applicable offer
            SpecialOffer appliedOffer = findBestOffer(plan, numberOfUsers);

            // Find all available offers for this number of users
            List<SpecialOffer> availableOffers = plan.getSpecialOffers() != null ?
                    plan.getSpecialOffers().stream()
                            .filter(offer -> numberOfUsers >= offer.getMinUserCount())
                            .sorted(Comparator.comparing(SpecialOffer::getDiscountPercentage).reversed())
                            .collect(Collectors.toList()) :
                    List.of();

            return CalculatePriceResponseDto.builder()
                    .planId(plan.getId())
                    .planName(plan.getName())
                    .pricePerUser(pricePerUser)
                    .numberOfUsers(numberOfUsers)
                    .total(total)
                    .discount(discount)
                    .finalPrice(finalPrice)
                    .appliedOffer(appliedOffer)
                    .availableOffers(availableOffers)
                    .build();
        } catch (Exception e) {
            log.error("Error calculating price: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("PRICE_CALCULATE_FAIL"));
        }
    }

    @Override
    public UserSubscriptionResponseDto submitUserSubscription(UserSubscriptionRequestDto requestDto) {
        try {
            String token = messagesUtil.getAuthToken();
            String userId = jwtTokenProvider.getIdFromToken(token);
            log.info("Submitting user subscription for user: {}", userId);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_SUBSCRIPTION_USER_NOT_FOUND")));

            SubscriptionPlan plan = subscriptionPlanRepository.findById(requestDto.getPlanId())
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_SUB_PLAN_NOT_FOUND")));

            // Check if user already has a pending subscription
            Optional<UserSubscription> existingSubscription = userSubscriptionRepository
                    .findByUserIdAndStatus(userId, PENDING);

            if (existingSubscription.isPresent()) {
                throw new AbstractException(messagesUtil.getMessage("USER_SUBSCRIPTION_PENDING_EXISTS"));
            }

            // Calculate pricing with offers
            CalculatePriceResponseDto calculatedPrice =
                    calculatePrice(CalculatePriceRequestDto.builder()
                            .numberOfUsers(requestDto.getNumberOfUsers())
                            .planId(requestDto.getPlanId())
                            .build());

            // Save file
            String filePath = saveSubscriptionFile(requestDto.getSubscriptionFile());

            UserSubscription userSubscription = UserSubscription.builder()
                    .user(user)
                    .userId(userId)
                    .plan(plan)
                    .planId(plan.getId())
                    .numberOfUsers(requestDto.getNumberOfUsers())
                    .usedUsers(0)
                    .remainingUsers(requestDto.getNumberOfUsers())
                    .total(calculatedPrice.getTotal())
                    .discount(calculatedPrice.getDiscount())
                    .finalPrice(calculatedPrice.getFinalPrice())
                    .filePath(filePath)
                    .status(PENDING)
                    .submissionDate(LocalDateTime.now())
                    .build();

            userSubscription = userSubscriptionRepository.save(userSubscription);

            return subscriptionMapper.toResponseDto(userSubscription);
        } catch (Exception e) {
            log.error("Error submitting user subscription: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("USER_SUBSCRIPTION_SUBMIT_FAIL"));
        }
    }

    @Override
    public UserSubscriptionResponseDto getUserSubscriptionByOwnerId(String ownerId) {
        try {
            log.info("Fetching user subscription for owner: {}", ownerId);
            UserSubscription userSubscription = userSubscriptionRepository.findByUserId(ownerId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_SUBSCRIPTION_NOT_FOUND")));
            return subscriptionMapper.toResponseDto(userSubscription);
        } catch (Exception e) {
            log.error("Error fetching user subscription: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("USER_SUBSCRIPTION_FETCH_FAIL"));
        }
    }

    @Override
    public UserSubscriptionResponseDto getUserSubscriptionById(String id) {
        try {
            log.info("Fetching user subscription with id: {}", id);
            UserSubscription userSubscription = userSubscriptionRepository.findById(id)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_SUBSCRIPTION_NOT_FOUND")));
            return subscriptionMapper.toResponseDto(userSubscription);
        } catch (Exception e) {
            log.error("Error fetching user subscription: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("USER_SUBSCRIPTION_FETCH_FAIL"));
        }
    }

    @Override
    public UserSubscriptionResponseDto updateUserSubscription(String id, UserSubscriptionRequestDto requestDto) {
        try {
            log.info("Updating user subscription with id: {}", id);

            UserSubscription userSubscription = userSubscriptionRepository.findById(id)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_SUBSCRIPTION_NOT_FOUND")));

            boolean planChanged = false;
            boolean numberOfUsersChanged = false;

            // Update plan if provided
            if (requestDto.getPlanId() != null && !requestDto.getPlanId().equals(userSubscription.getPlanId())) {
                SubscriptionPlan plan = subscriptionPlanRepository.findById(requestDto.getPlanId())
                        .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_SUB_PLAN_NOT_FOUND")));
                userSubscription.setPlan(plan);
                userSubscription.setPlanId(plan.getId());
                planChanged = true;
            }

            // Update number of users if provided
            if (requestDto.getNumberOfUsers() > 0 && requestDto.getNumberOfUsers() != userSubscription.getNumberOfUsers()) {
                userSubscription.setNumberOfUsers(requestDto.getNumberOfUsers());
                numberOfUsersChanged = true;
            }

            // Recalculate price if plan or number of users changed
            if (planChanged || numberOfUsersChanged) {
                SubscriptionPlan plan = userSubscription.getPlan();
                int numberOfUsers = userSubscription.getNumberOfUsers();
                double total = plan.getPricePerUser() * numberOfUsers;
                double discount = calculateDiscount(plan, numberOfUsers);
                double finalPrice = total - discount;

                userSubscription.setTotal(total);
                userSubscription.setDiscount(discount);
                userSubscription.setFinalPrice(finalPrice);
                userSubscription.setRemainingUsers(numberOfUsers - userSubscription.getUsedUsers());
            }

            // Update file if provided
            if (requestDto.getSubscriptionFile() != null && !requestDto.getSubscriptionFile().isEmpty()) {
                // Delete old file if exists
                if (userSubscription.getFilePath() != null) {
                    try {
                        Files.deleteIfExists(Paths.get(userSubscription.getFilePath()));
                    } catch (IOException e) {
                        log.warn("Failed to delete old file: {}", e.getMessage());
                    }
                }

                // Save new file
                String filePath = saveSubscriptionFile(requestDto.getSubscriptionFile());
                userSubscription.setFilePath(filePath);
            }

            userSubscription = userSubscriptionRepository.save(userSubscription);

            return subscriptionMapper.toResponseDto(userSubscription);
        } catch (Exception e) {
            log.error("Error updating user subscription: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("USER_SUBSCRIPTION_UPDATE_FAIL"));
        }
    }

    @Override
    public void deleteUserSubscription(String id) {
        try {
            log.info("Deleting user subscription with id: {}", id);

            UserSubscription userSubscription = userSubscriptionRepository.findById(id)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_SUBSCRIPTION_NOT_FOUND")));

            // Delete file if exists
            if (userSubscription.getFilePath() != null) {
                try {
                    Files.deleteIfExists(Paths.get(userSubscription.getFilePath()));
                } catch (IOException e) {
                    log.warn("Failed to delete file: {}", e.getMessage());
                }
            }

            userSubscriptionRepository.delete(userSubscription);
            log.info("User subscription deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting user subscription: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("USER_SUBSCRIPTION_DELETE_FAIL"));
        }
    }


    @Override
    public UserSubscriptionResponseDto createSubscription(String userId, CreateSubscriptionRequestDto requestDto) {
        try {
            log.info("Creating subscription for user: {} with plan: {} and {} users",
                    userId, requestDto.getPlanId(), requestDto.getNumberOfUsers());

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_SUBSCRIPTION_USER_NOT_FOUND")));

            SubscriptionPlan plan = subscriptionPlanRepository.findById(requestDto.getPlanId())
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_SUB_PLAN_NOT_FOUND")));

            // Check if user already has an active subscription
            Optional<UserSubscription> existingSubscription = userSubscriptionRepository.findByUserId(userId);
            if (existingSubscription.isPresent()) {
                throw new AbstractException(messagesUtil.getMessage("SUBSCRIPTION_ACTIVE_EXISTS"));
            }

            // Calculate pricing with offers
            double total = plan.getPricePerUser() * requestDto.getNumberOfUsers();
            double discount = calculateDiscount(plan, requestDto.getNumberOfUsers());
            double finalPrice = total - discount;

            UserSubscription subscription = UserSubscription.builder()
                    .user(user)
                    .userId(userId)
                    .plan(plan)
                    .planId(plan.getId())
                    .numberOfUsers(requestDto.getNumberOfUsers())
                    .usedUsers(0)
                    .remainingUsers(requestDto.getNumberOfUsers())
                    .total(total)
                    .discount(discount)
                    .finalPrice(finalPrice)
                    .build();

            subscription = userSubscriptionRepository.save(subscription);

            return subscriptionMapper.toResponseDto(subscription);
        } catch (Exception e) {
            log.error("Error creating subscription: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("SUBSCRIPTION_CREATE_FAIL"));
        }
    }

    @Override
    public UserSubscriptionResponseDto getUserSubscription() {
        try {
            String token = messagesUtil.getAuthToken();
            String userId = jwtTokenProvider.getIdFromToken(token);
            log.info("Fetching subscription for user: {}", userId);
            UserSubscription subscription = userSubscriptionRepository.findByUserId(userId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("SUBSCRIPTION_NOT_FOUND")));
            return subscriptionMapper.toResponseDto(subscription);
        } catch (Exception e) {
            log.error("Error fetching subscription: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("SUBSCRIPTION_FETCH_FAIL"));
        }
    }

    @Override
    public UserSubscriptionResponseDto updateUsedUsers(String subscriptionId, int usedUsers) {
        try {
            log.info("Updating used users for subscription: {} to {}", subscriptionId, usedUsers);
            UserSubscription subscription = userSubscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("SUBSCRIPTION_NOT_FOUND")));

            if (usedUsers > subscription.getNumberOfUsers()) {
                throw new AbstractException(messagesUtil.getMessage("SUBSCRIPTION_USED_USERS_EXCEED"));
            }

            subscription.setUsedUsers(usedUsers);
            subscription.setRemainingUsers(subscription.getNumberOfUsers() - usedUsers);

            subscription = userSubscriptionRepository.save(subscription);

            return subscriptionMapper.toResponseDto(subscription);
        } catch (Exception e) {
            log.error("Error updating used users: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("SUBSCRIPTION_USED_USERS_UPDATE_FAIL"));
        }
    }


    @Override
    public Page<UserSubscriptionResponseDto> getAllPendingUserSubscriptions(Pageable pageable) {
        try {
            log.info("Fetching all pending user subscriptions");
            Page<UserSubscription> userSubscriptions = userSubscriptionRepository.findByStatus(pageable,PENDING);

            return subscriptionMapper.toResponsePages(userSubscriptions);
        } catch (Exception e) {
            log.error("Error fetching pending user subscriptions: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("USER_SUBSCRIPTION_PENDING_FETCH_FAIL"));
        }
    }

    @Override
    public UserSubscriptionResponseDto approveUserSubscription(String userSubscriptionId) {
        try {
            log.info("Approving user subscription: {}", userSubscriptionId);

            UserSubscription userSubscription = userSubscriptionRepository.findById(userSubscriptionId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_SUBSCRIPTION_NOT_FOUND")));

            if (userSubscription.getStatus() != PENDING) {
                throw new AbstractException(messagesUtil.getMessage("USER_SUBSCRIPTION_STATUS_INVALID"));
            }

            // Update user subscription status
            userSubscription.setStatus(UserSubscriptionStatus.APPROVED);
            userSubscription = userSubscriptionRepository.save(userSubscription);

            // Get the user
            User user = userRepository.findById(userSubscription.getUserId())
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_NOT_FOUND")));

            // Update user's account status to ACTIVE
            user.setAccountStatus(AccountStatus.ACTIVE);

            // Link user to the selected subscription plan
            if (userSubscription.getPlan() != null) {
                user.setSubscriptionPlan(userSubscription.getPlan());
            }

            userRepository.save(user);

            log.info("User subscription approved successfully. User {} activated with plan {}",
                    user.getId(), userSubscription.getPlanId());

            return subscriptionMapper.toResponseDto(userSubscription);
        } catch (Exception e) {
            log.error("Error approving user subscription: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("USER_SUBSCRIPTION_APPROVE_FAIL"));
        }
    }

    @Override
    public UserSubscriptionResponseDto rejectUserSubscription(String userSubscriptionId, String reason) {
        try {
            log.info("Rejecting user subscription: {} with reason: {}", userSubscriptionId, reason);

            UserSubscription userSubscription = userSubscriptionRepository.findById(userSubscriptionId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_SUBSCRIPTION_NOT_FOUND")));

            if (userSubscription.getStatus() != PENDING) {
                throw new AbstractException(messagesUtil.getMessage("USER_SUBSCRIPTION_STATUS_INVALID"));
            }

            userSubscription.setStatus(UserSubscriptionStatus.REJECTED);
            userSubscription = userSubscriptionRepository.save(userSubscription);

            return subscriptionMapper.toResponseDto(userSubscription);
        } catch (Exception e) {
            log.error("Error rejecting user subscription: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("USER_SUBSCRIPTION_REJECT_FAIL"));
        }
    }



    private String saveSubscriptionFile(MultipartFile file) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }


    private double calculateDiscount(SubscriptionPlan plan, int numberOfUsers) {
        if (plan.getSpecialOffers() == null || plan.getSpecialOffers().isEmpty()) {
            return 0.0;
        }

        // Find the best applicable offer (highest discount for the given user count)
        Optional<SpecialOffer> bestOffer = plan.getSpecialOffers().stream()
                .filter(offer -> numberOfUsers >= offer.getMinUserCount())
                .max(Comparator.comparing(SpecialOffer::getDiscountPercentage));

        if (bestOffer.isPresent()) {
            double total = plan.getPricePerUser() * numberOfUsers;
            return total * (bestOffer.get().getDiscountPercentage() / 100.0);
        }

        return 0.0;
    }


    private SpecialOffer findBestOffer(SubscriptionPlan plan, int numberOfUsers) {
        if (plan.getSpecialOffers() == null || plan.getSpecialOffers().isEmpty()) {
            return null;
        }

        return plan.getSpecialOffers().stream()
                .filter(offer -> numberOfUsers >= offer.getMinUserCount())
                .max(Comparator.comparing(SpecialOffer::getDiscountPercentage))
                .orElse(null);
    }
}
