package com.rawneeded.service.impl;

import com.rawneeded.dto.subscription.CalculatePriceRequestDto;
import com.rawneeded.dto.subscription.CalculatePriceResponseDto;
import com.rawneeded.dto.subscription.UserSubscriptionRequestDto;
import com.rawneeded.dto.subscription.UserSubscriptionResponseDto;
import com.rawneeded.dto.MailDto;
import com.rawneeded.enumeration.AccountStatus;
import com.rawneeded.enumeration.BillingFrequency;
import com.rawneeded.enumeration.NotificationType;
import com.rawneeded.enumeration.PlanFeatures;
import com.rawneeded.enumeration.PlanType;
import com.rawneeded.enumeration.Role;
import com.rawneeded.enumeration.TemplateName;
import com.rawneeded.enumeration.UserSubscriptionStatus;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.jwt.JwtTokenProvider;
import com.rawneeded.mapper.UserSubscriptionMapper;
import com.rawneeded.model.PlanFeature;
import com.rawneeded.model.ProductSearchesConfig;
import com.rawneeded.model.SpecialOffer;
import com.rawneeded.model.SubscriptionPlan;
import com.rawneeded.dto.subscription.AddSearchesRequestDto;
import com.rawneeded.dto.subscription.AddSearchesSubmitDto;
import com.rawneeded.model.AddSearchesRequest;
import com.rawneeded.model.User;
import com.rawneeded.model.UserSubscription;
import com.rawneeded.repository.AddSearchesRequestRepository;
import com.rawneeded.repository.SubscriptionPlanRepository;
import com.rawneeded.repository.UserRepository;
import com.rawneeded.repository.UserSubscriptionRepository;
import com.rawneeded.service.INotificationService;
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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.rawneeded.enumeration.UserSubscriptionStatus.APPROVED;
import static com.rawneeded.enumeration.UserSubscriptionStatus.PENDING;
import static com.rawneeded.enumeration.UserSubscriptionStatus.REJECTED;

@Slf4j
@Service
@AllArgsConstructor
public class UserSubscriptionServiceImpl implements IUserSubscriptionService {

    private static final String UPLOAD_DIR = "uploads/user-subscriptions/";
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final AddSearchesRequestRepository addSearchesRequestRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserSubscriptionMapper subscriptionMapper;
    private final UserRepository userRepository;
    private final MessagesUtil messagesUtil;
    private final JwtTokenProvider jwtTokenProvider;
    private final INotificationService notificationService;
    private final NotificationService emailService;

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
                    .selectedFeatures(requestDto.getSelectedFeatures())
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
                    .orElse(null);
            if (subscription != null) {
                return subscriptionMapper.toResponseDto(subscription);
            } else {
                return null;
            }

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
    public Page<UserSubscriptionResponseDto> getApprovedUserSubscriptions(Pageable pageable) {
        try {
            log.info("Fetching approved user subscriptions");
            Page<UserSubscription> userSubscriptions = userSubscriptionRepository.findByStatus(pageable, APPROVED);
            return subscriptionMapper.toResponsePages(userSubscriptions);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching approved user subscriptions: {}", e.getMessage());
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
    public AddSearchesRequestDto submitAddSearchesRequest(AddSearchesSubmitDto dto) {
        String userId = jwtTokenProvider.getOwnerIdFromToken(messagesUtil.getAuthToken());
        UserSubscription subscription = userSubscriptionRepository.findFirstByUserId(userId)
                .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("SUBSCRIPTION_NOT_FOUND")));
        if (subscription.getStatus() != APPROVED) {
            throw new AbstractException(messagesUtil.getMessage("USER_SUB_PLAN_NOT_FOUND"));
        }
        if (subscription.getPlanId() == null) {
            throw new AbstractException(messagesUtil.getMessage("USER_SUB_PLAN_NOT_FOUND"));
        }
        SubscriptionPlan plan = subscriptionPlanRepository.findById(subscription.getPlanId())
                .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_SUB_PLAN_NOT_FOUND")));
        if (plan.getProductSearchesConfig() == null || plan.getProductSearchesConfig().getPricePerSearch() == null) {
            throw new AbstractException("Plan does not support adding searches");
        }
        double totalPrice = dto.getNumberOfSearches() * plan.getProductSearchesConfig().getPricePerSearch();
        AddSearchesRequest request = AddSearchesRequest.builder()
                .userId(userId)
                .subscription(subscription)
                .subscriptionId(subscription.getId())
                .numberOfSearches(dto.getNumberOfSearches())
                .totalPrice(totalPrice)
                .receiptFilePath(dto.getReceiptFile() != null ? dto.getReceiptFile() : "")
                .status(PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        request = addSearchesRequestRepository.save(request);
        User user = userRepository.findById(userId).orElse(null);
        String userName = user != null && user.getName() != null ? user.getName() : userId;
        notifyAdminsOfPendingRequest(
                request.getId(),
                "ADD_SEARCHES_REQUEST",
                "NOTIFICATION_PENDING_ADD_SEARCHES_TITLE",
                "NOTIFICATION_PENDING_ADD_SEARCHES_MESSAGE",
                "EMAIL_SUBJECT_PENDING_ADD_SEARCHES",
                userName
        );
        return toAddSearchesRequestDto(request, subscription, plan);
    }

    @Override
    public double calculateAddSearchesPrice(int numberOfSearches) {
        String userId = jwtTokenProvider.getOwnerIdFromToken(messagesUtil.getAuthToken());
        UserSubscription subscription = userSubscriptionRepository.findFirstByUserId(userId)
                .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("SUBSCRIPTION_NOT_FOUND")));
        if (subscription.getStatus() != APPROVED || subscription.getPlanId() == null) {
            throw new AbstractException(messagesUtil.getMessage("USER_SUB_PLAN_NOT_FOUND"));
        }
        SubscriptionPlan plan = subscriptionPlanRepository.findById(subscription.getPlanId())
                .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_SUB_PLAN_NOT_FOUND")));
        if (plan.getProductSearchesConfig() == null || plan.getProductSearchesConfig().getPricePerSearch() == null) {
            throw new AbstractException("Plan does not support adding searches");
        }
        return numberOfSearches * plan.getProductSearchesConfig().getPricePerSearch();
    }

    @Override
    public Page<AddSearchesRequestDto> getPendingAddSearchesRequests(Pageable pageable) {
        Page<AddSearchesRequest> page = addSearchesRequestRepository.findByStatusOrderByCreatedAtDesc(PENDING, pageable);
        return page.map(req -> {
            UserSubscription sub = req.getSubscription() != null ? req.getSubscription() : userSubscriptionRepository.findById(req.getSubscriptionId()).orElse(null);
            SubscriptionPlan plan = sub != null && sub.getPlanId() != null ? subscriptionPlanRepository.findById(sub.getPlanId()).orElse(null) : null;
            User user = sub != null ? userRepository.findById(sub.getUserId()).orElse(null) : null;
            return AddSearchesRequestDto.builder()
                    .id(req.getId())
                    .userId(req.getUserId())
                    .subscriptionId(req.getSubscriptionId())
                    .planName(plan != null ? plan.getName() : null)
                    .userOrganizationName(user != null ? user.getOrganizationName() : null)
                    .numberOfSearches(req.getNumberOfSearches())
                    .totalPrice(req.getTotalPrice())
                    .receiptFilePath(req.getReceiptFilePath())
                    .status(req.getStatus())
                    .createdAt(req.getCreatedAt())
                    .build();
        });
    }

    @Override
    public AddSearchesRequestDto approveAddSearchesRequest(String requestId) {
        AddSearchesRequest request = addSearchesRequestRepository.findById(requestId)
                .orElseThrow(() -> new AbstractException("Add searches request not found"));
        if (request.getStatus() != PENDING) {
            throw new AbstractException("Request is not pending");
        }
        UserSubscription subscription = userSubscriptionRepository.findById(request.getSubscriptionId())
                .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("SUBSCRIPTION_NOT_FOUND")));
        int currentTotal = subscription.getNumberOfSearches() != null ? subscription.getNumberOfSearches() : 0;
        int currentRemaining = subscription.getRemainingSearches() != null ? subscription.getRemainingSearches() : 0;
        subscription.setNumberOfSearches(currentTotal + request.getNumberOfSearches());
        subscription.setRemainingSearches(currentRemaining + request.getNumberOfSearches());
        userSubscriptionRepository.save(subscription);
        request.setStatus(APPROVED);
        request = addSearchesRequestRepository.save(request);
        SubscriptionPlan plan = subscriptionPlanRepository.findById(subscription.getPlanId()).orElse(null);
        return toAddSearchesRequestDto(request, subscription, plan);
    }

    @Override
    public AddSearchesRequestDto rejectAddSearchesRequest(String requestId, String reason) {
        AddSearchesRequest request = addSearchesRequestRepository.findById(requestId)
                .orElseThrow(() -> new AbstractException("Add searches request not found"));
        if (request.getStatus() != PENDING) {
            throw new AbstractException("Request is not pending");
        }
        request.setStatus(REJECTED);
        request = addSearchesRequestRepository.save(request);
        UserSubscription subscription = userSubscriptionRepository.findById(request.getSubscriptionId()).orElse(null);
        SubscriptionPlan plan = subscription != null ? subscriptionPlanRepository.findById(subscription.getPlanId()).orElse(null) : null;
        return toAddSearchesRequestDto(request, subscription, plan);
    }

    private AddSearchesRequestDto toAddSearchesRequestDto(AddSearchesRequest req, UserSubscription sub, SubscriptionPlan plan) {
        AtomicReference<String> userOrg = new AtomicReference<>(null);
        if (sub != null && sub.getUserId() != null) {
            userRepository.findById(sub.getUserId()).ifPresent(u -> userOrg.set(u.getOrganizationName()));
        }
        return AddSearchesRequestDto.builder()
                .id(req.getId())
                .userId(req.getUserId())
                .subscriptionId(req.getSubscriptionId())
                .planName(plan != null ? plan.getName() : null)
                .userOrganizationName(userOrg.get() != null ? userOrg.get() : "")
                .numberOfSearches(req.getNumberOfSearches())
                .totalPrice(req.getTotalPrice())
                .receiptFilePath(req.getReceiptFilePath())
                .status(req.getStatus())
                .createdAt(req.getCreatedAt())
                .build();
    }

    @Override
    public boolean deductSearchAndAddPoints(String userId) {
        try {
            log.info("Deducting search (or using point) for user: {}", userId);
            
            UserSubscription subscription = userSubscriptionRepository.findFirstByUserId(userId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("SUBSCRIPTION_NOT_FOUND")));
            
            // Check if user has remaining searches
            if (subscription.getRemainingSearches() != null && subscription.getRemainingSearches() > 0) {
                subscription.setRemainingSearches(subscription.getRemainingSearches() - 1);
                userSubscriptionRepository.save(subscription);
                return true;
            }
            
            // If no remaining searches, check if user has points (1 point = 1 search)
            if (subscription.getPoints() != null && subscription.getPoints() > 0) {
                subscription.setPoints(subscription.getPoints() - 1);
                userSubscriptionRepository.save(subscription);
                return true;
            }
            
            return false;
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deducting search: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("SEARCH_DEDUCT_FAIL"));
        }
    }

    private void notifyAdminsOfPendingRequest(String entityId, String entityType, String titleKey, String messageKey,
                                             String emailSubjectKey, String userName) {
        try {
            List<User> admins = userRepository.findAllByRole(Role.SUPER_ADMIN);
            String subject = messagesUtil.getMessage(titleKey);
            String description = messagesUtil.getMessage(messageKey);
            for (User admin : admins) {
                notificationService.sendNotificationToUser(
                        admin.getId(),
                        NotificationType.GENERAL,
                        titleKey,
                        messageKey,
                        entityId,
                        entityType,
                        userName != null ? userName : ""
                );
                if (admin.getEmail() != null && !admin.getEmail().isEmpty()) {
                    try {
                        emailService.sendEmail(MailDto.builder()
                                .toEmail(admin.getEmail())
                                .subject(messagesUtil.getMessage(emailSubjectKey))
                                .templateName(TemplateName.COMPLAINT_CREATED_ADMIN)
                                .model(Map.of(
                                        "userName", userName != null ? userName : "",
                                        "subject", subject != null ? subject : "",
                                        "description", description != null ? description : ""
                                ))
                                .build());
                    } catch (Exception e) {
                        log.error("Failed to send pending-request email to admin: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error sending notifications/emails to admins: {}", e.getMessage());
        }
    }

    @Override
    public void addPointForSupplierResponse(String customerUserId) {
        try {
            userSubscriptionRepository.findFirstByUserId(customerUserId).ifPresent(sub -> {
                sub.setPoints((sub.getPoints() != null ? sub.getPoints() : 0) + 1);
                userSubscriptionRepository.save(sub);
                log.info("Added 1 point for supplier response to customer: {}", customerUserId);
            });
        } catch (Exception e) {
            log.warn("Could not add point for supplier response (customer {}): {}", customerUserId, e.getMessage());
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





