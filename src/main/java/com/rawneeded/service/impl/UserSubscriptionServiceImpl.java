package com.rawneeded.service.impl;

import com.rawneeded.dto.subscription.CalculatePriceRequestDto;
import com.rawneeded.dto.subscription.CalculatePriceResponseDto;
import com.rawneeded.dto.subscription.UserSubscriptionRequestDto;
import com.rawneeded.dto.subscription.UserSubscriptionResponseDto;
import com.rawneeded.enumeration.AccountStatus;
import com.rawneeded.enumeration.BillingFrequency;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.rawneeded.enumeration.UserSubscriptionStatus.APPROVED;
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
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error calculating price: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("PRICE_CALCULATE_FAIL"));
        }
    }

    @Override
    public UserSubscriptionResponseDto submitUserSubscription(UserSubscriptionRequestDto requestDto) {
        try {
            String token = messagesUtil.getAuthToken();
            String userId = jwtTokenProvider.getOwnerIdFromToken(token);
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
            String filePath = requestDto.getSubscriptionFile();

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
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error submitting user subscription: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("USER_SUBSCRIPTION_SUBMIT_FAIL"));
        }
    }


    @Override
    public User putUserOnFreeTrail(User user) {
        try{
            log.info("Putting user on free trail: {}", user.getId());
            // Find a plan with price 0 (free plan)
            List<SubscriptionPlan> freePlans = subscriptionPlanRepository.findAll().stream()
                    .filter(plan -> plan.getPricePerUser() == 0.0 && plan.isActive())
                    .toList();
            
            if (freePlans.isEmpty()) {
                log.warn("No free plan found (price = 0). Skipping free trial subscription for user: {}", user.getId());
                return user;
            }
            
            SubscriptionPlan freePlan = freePlans.get(0);
            UserSubscription userSubscription = UserSubscription.builder()
                    .user(user)
                    .userId(user.getId())
                    .plan(freePlan)
                    .planId(freePlan.getId())
                    .numberOfUsers(1)
                    .usedUsers(1)
                    .remainingUsers(0)
                    .total(0.0)
                    .discount(0.0)
                    .finalPrice(0.0)
                    .status(APPROVED)
                    .subscriptionDate(LocalDateTime.now())
                    .subscriptionDate(LocalDateTime.now())
                    .expiryDate(LocalDateTime.now().plusMonths(1))
                    .build();

            userSubscription = userSubscriptionRepository.save(userSubscription);
            user.setSubscription(userSubscription);
            user.setAccountStatus(AccountStatus.ACTIVE);
            return user;
        }catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.info("Error putting user on free trail: {}", e.getMessage());
            return user;
        }
    }


    @Override
    public UserSubscriptionResponseDto getUserSubscription() {
        try {
            String token = messagesUtil.getAuthToken();
            String userId = jwtTokenProvider.getOwnerIdFromToken(token);
            log.info("Fetching subscription for user: {}", userId);
            UserSubscription subscription = userSubscriptionRepository.findFirstByUserId(userId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("SUBSCRIPTION_NOT_FOUND")));
            return subscriptionMapper.toResponseDto(subscription);
        }catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching subscription: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("SUBSCRIPTION_FETCH_FAIL"));
        }
    }

    @Override
    public void updateUsedUsers(String subscriptionId, boolean add) {
        try {

            UserSubscription subscription = userSubscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("SUBSCRIPTION_NOT_FOUND")));

            int newUsedUsers = add ? subscription.getUsedUsers() + 1 : subscription.getUsedUsers() - 1;

            if (newUsedUsers > subscription.getNumberOfUsers()) {
                throw new AbstractException(messagesUtil.getMessage("SUBSCRIPTION_USED_USERS_EXCEED"));
            }

            subscription.setUsedUsers(newUsedUsers);
            subscription.setRemainingUsers(subscription.getNumberOfUsers() - newUsedUsers);

            subscription = userSubscriptionRepository.save(subscription);

            subscriptionMapper.toResponseDto(subscription);
        }catch (AbstractException e) {
            throw e;
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
        }catch (AbstractException e) {
            throw e;
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

            // Set subscription date and expiry date

            BillingFrequency billingFrequency = userSubscription.getPlan().getBillingFrequency();
            LocalDateTime expiryDate = billingFrequency.equals(BillingFrequency.MONTHLY) ? LocalDateTime.now().plusMonths(1) :
                    billingFrequency.equals(BillingFrequency.QUARTERLY) ? LocalDateTime.now().plusMonths(3) :
                            LocalDateTime.now().plusYears(1);

            userSubscription.setSubscriptionDate(LocalDateTime.now());
            userSubscription.setExpiryDate(expiryDate);
            userSubscription = userSubscriptionRepository.save(userSubscription);


            // Update user's account status and subscription
            activateUserAndCleanupSubscriptions(userSubscription);

            return subscriptionMapper.toResponseDto(userSubscription);
        }catch (AbstractException e) {
            throw e;
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
        }catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error rejecting user subscription: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("USER_SUBSCRIPTION_REJECT_FAIL"));
        }
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


    @Async
    public void activateUserAndCleanupSubscriptions(UserSubscription subscription) {
        try {
            User user = userRepository.findById(subscription.getUserId())
                    .orElseThrow(() -> new AbstractException("USER_NOT_FOUND"));

            user.setAccountStatus(AccountStatus.ACTIVE);
            user.setSubscription(subscription);
            userRepository.save(user);

            List<UserSubscription> oldSubscriptions =
                    userSubscriptionRepository.findByUserIdAndIdNot(user.getId(), subscription.getId());

            userSubscriptionRepository.deleteAll(oldSubscriptions);

        } catch (Exception e) {
            log.error("Error activating user and cleaning up subscriptions: {}", e.getMessage());

        }
    }

}

