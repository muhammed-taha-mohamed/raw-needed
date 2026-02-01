package com.rawneeded.service.impl;

import com.rawneeded.dto.subscription.CalculatePriceRequestDto;
import com.rawneeded.dto.subscription.CalculatePriceResponseDto;
import com.rawneeded.dto.subscription.UserSubscriptionRequestDto;
import com.rawneeded.dto.subscription.UserSubscriptionResponseDto;
import com.rawneeded.enumeration.AccountStatus;
import com.rawneeded.enumeration.BillingFrequency;
import com.rawneeded.enumeration.PlanFeatures;
import com.rawneeded.enumeration.PlanType;
import com.rawneeded.enumeration.UserSubscriptionStatus;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.jwt.JwtTokenProvider;
import com.rawneeded.mapper.UserSubscriptionMapper;
import com.rawneeded.model.PlanFeature;
import com.rawneeded.model.ProductSearchesConfig;
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
            double basePrice = 0.0;
            double searchesPrice = 0.0;
            double featuresPrice = 0.0;
            
            // Calculate base price based on a plan type
            if (plan.getPlanType() == PlanType.SUPPLIER) {
                // For suppliers: base subscription price
                basePrice = plan.getPricePerUser() * numberOfUsers;
            } else if (plan.getPlanType() == PlanType.CUSTOMER) {
                // For customers: price per user
                basePrice = plan.getPricePerUser() * numberOfUsers;
                
                // Calculate product searches price if provided
                if (requestDto.getNumberOfSearches() != null && plan.getProductSearchesConfig() != null) {
                    Integer numberOfSearches = requestDto.getNumberOfSearches();
                    ProductSearchesConfig config = plan.getProductSearchesConfig();
                    
                    // Validate searches count
                    if (config.getUnlimited() != null && config.getUnlimited()) {
                        // Unlimited searches - use a fixed price or 0
                        searchesPrice = config.getPricePerSearch() != null ? config.getPricePerSearch() : 0.0;
                    } else if (config.getFrom() != null && config.getTo() != null) {
                        // Validate range
                        if (numberOfSearches < config.getFrom() || numberOfSearches > config.getTo()) {
                            throw new AbstractException("Number of searches must be between " + config.getFrom() + " and " + config.getTo());
                        }
                        searchesPrice = numberOfSearches * (config.getPricePerSearch() != null ? config.getPricePerSearch() : 0.0);
                    } else if (config.getPricePerSearch() != null) {
                        searchesPrice = numberOfSearches * config.getPricePerSearch();
                    }
                }
            } else {
                // For BOTH type
                basePrice = plan.getPricePerUser() * numberOfUsers;
            }
            
            // Calculate features price
            if (requestDto.getSelectedFeatures() != null && !requestDto.getSelectedFeatures().isEmpty() 
                    && plan.getFeatures() != null && !plan.getFeatures().isEmpty()) {
                for (PlanFeatures selectedFeature : requestDto.getSelectedFeatures()) {
                    // Find the feature in plan's feature list
                    Optional<PlanFeature> planFeature = plan.getFeatures().stream()
                            .filter(f -> f.getFeature() == selectedFeature)
                            .findFirst();
                    
                    if (planFeature.isPresent() && planFeature.get().getPrice() != null) {
                        featuresPrice += planFeature.get().getPrice();
                    }
                }
            }
            
            double total = basePrice + searchesPrice + featuresPrice;

            // Calculate the discount and find the best offer
            double discount = calculateDiscount(plan, numberOfUsers, total);
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
                    .pricePerUser(plan.getPricePerUser())
                    .numberOfUsers(numberOfUsers)
                    .basePrice(basePrice)
                    .numberOfSearches(requestDto.getNumberOfSearches())
                    .searchesPrice(searchesPrice)
                    .featuresPrice(featuresPrice)
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
                            .numberOfSearches(requestDto.getNumberOfSearches())
                            .selectedFeatures(requestDto.getSelectedFeatures())
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
                    // Initialize searches and points for customer plans
                    .numberOfSearches(requestDto.getNumberOfSearches())
                    .remainingSearches(requestDto.getNumberOfSearches())
                    .points(0)
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


    private double calculateDiscount(SubscriptionPlan plan, int numberOfUsers, double total) {
        if (plan.getSpecialOffers() == null || plan.getSpecialOffers().isEmpty()) {
            return 0.0;
        }

        // Find the best applicable offer (highest discount for the given user count)
        Optional<SpecialOffer> bestOffer = plan.getSpecialOffers().stream()
                .filter(offer -> numberOfUsers >= offer.getMinUserCount())
                .max(Comparator.comparing(SpecialOffer::getDiscountPercentage));

        if (bestOffer.isPresent()) {
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


    @Override
    public boolean deductSearchAndAddPoints(String userId) {
        try {
            log.info("Deducting search and adding points for user: {}", userId);
            
            UserSubscription subscription = userSubscriptionRepository.findFirstByUserId(userId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("SUBSCRIPTION_NOT_FOUND")));
            
            // Check if user has remaining searches
            if (subscription.getRemainingSearches() != null && subscription.getRemainingSearches() > 0) {
                // Deduct one search
                subscription.setRemainingSearches(subscription.getRemainingSearches() - 1);
                // Add points (1 point per search)
                subscription.setPoints((subscription.getPoints() != null ? subscription.getPoints() : 0) + 1);
                userSubscriptionRepository.save(subscription);
                return true;
            }
            
            // If no remaining searches, check if user has points
            if (subscription.getPoints() != null && subscription.getPoints() > 0) {
                // Use points for search (1 point = 1 search)
                subscription.setPoints(subscription.getPoints() - 1);
                userSubscriptionRepository.save(subscription);
                return true;
            }
            
            // No searches and no points
            return false;
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deducting search and adding points: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("SEARCH_DEDUCT_FAIL"));
        }
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





