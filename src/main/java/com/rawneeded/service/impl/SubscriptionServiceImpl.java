package com.rawneeded.service.impl;

import com.rawneeded.dto.subscription.CreatePlanRequestDto;
import com.rawneeded.dto.subscription.CreateSubscriptionRequestDto;
import com.rawneeded.dto.subscription.QuotationRequestDto;
import com.rawneeded.dto.subscription.QuotationResponseDto;
import com.rawneeded.dto.subscription.SubscriptionPlanResponseDto;
import com.rawneeded.dto.subscription.UpdatePlanRequestDto;
import com.rawneeded.dto.subscription.UserSubscriptionResponseDto;
import com.rawneeded.enumeration.QuotationStatus;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.model.PaymentQuotation;
import com.rawneeded.model.SpecialOffer;
import com.rawneeded.model.SubscriptionPlan;
import com.rawneeded.model.User;
import com.rawneeded.model.UserSubscription;
import com.rawneeded.repository.PaymentQuotationRepository;
import com.rawneeded.repository.SubscriptionPlanRepository;
import com.rawneeded.repository.UserRepository;
import com.rawneeded.repository.UserSubscriptionRepository;
import com.rawneeded.service.ISubscriptionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class SubscriptionServiceImpl implements ISubscriptionService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PaymentQuotationRepository paymentQuotationRepository;
    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private static final String UPLOAD_DIR = "uploads/quotations/";

    @Override
    public List<SubscriptionPlanResponseDto> getAllPlans() {
        try {
            log.info("Fetching all subscription plans");
            List<SubscriptionPlan> plans = subscriptionPlanRepository.findAll();
            return plans.stream()
                    .map(this::mapToResponseDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching subscription plans: {}", e.getMessage());
            throw new AbstractException("Failed to fetch subscription plans: " + e.getMessage());
        }
    }

    @Override
    public SubscriptionPlanResponseDto getPlanById(String planId) {
        try {
            log.info("Fetching subscription plan with id: {}", planId);
            SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                    .orElseThrow(() -> new AbstractException("Subscription plan not found"));
            return mapToResponseDto(plan);
        } catch (Exception e) {
            log.error("Error fetching subscription plan: {}", e.getMessage());
            throw new AbstractException("Failed to fetch subscription plan: " + e.getMessage());
        }
    }

    @Override
    public SubscriptionPlanResponseDto createPlan(CreatePlanRequestDto requestDto) {
        try {
            log.info("Creating new subscription plan: {}", requestDto.getName());
            
            // Check if plan with same name already exists
            List<SubscriptionPlan> existingPlans = subscriptionPlanRepository.findAll();
            boolean nameExists = existingPlans.stream()
                    .anyMatch(plan -> plan.getName().equalsIgnoreCase(requestDto.getName()));
            
            if (nameExists) {
                throw new AbstractException("A plan with this name already exists");
            }
            
            SubscriptionPlan plan = SubscriptionPlan.builder()
                    .name(requestDto.getName())
                    .pricePerUser(requestDto.getPricePerUser())
                    .description(requestDto.getDescription())
                    .billingFrequency(requestDto.getBillingFrequency())
                    .specialOffers(requestDto.getSpecialOffers())
                    .build();
            
            plan = subscriptionPlanRepository.save(plan);
            log.info("Subscription plan created successfully with id: {}", plan.getId());
            
            return mapToResponseDto(plan);
        } catch (Exception e) {
            log.error("Error creating subscription plan: {}", e.getMessage());
            throw new AbstractException("Failed to create subscription plan: " + e.getMessage());
        }
    }

    @Override
    public SubscriptionPlanResponseDto updatePlan(String planId, UpdatePlanRequestDto requestDto) {
        try {
            log.info("Updating subscription plan with id: {}", planId);
            
            SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                    .orElseThrow(() -> new AbstractException("Subscription plan not found"));
            
            // Check if the name is being changed and if new name already exists
            if (requestDto.getName() != null && !requestDto.getName().equals(plan.getName())) {
                List<SubscriptionPlan> existingPlans = subscriptionPlanRepository.findAll();
                boolean nameExists = existingPlans.stream()
                        .anyMatch(p -> !p.getId().equals(planId) && 
                                p.getName().equalsIgnoreCase(requestDto.getName()));
                
                if (nameExists) {
                    throw new AbstractException("A plan with this name already exists");
                }
                plan.setName(requestDto.getName());
            }
            
            // Update fields if provided
            if (requestDto.getPricePerUser() != null) {
                plan.setPricePerUser(requestDto.getPricePerUser());
            }
            
            if (requestDto.getDescription() != null) {
                plan.setDescription(requestDto.getDescription());
            }
            
            if (requestDto.getBillingFrequency() != null) {
                plan.setBillingFrequency(requestDto.getBillingFrequency());
            }
            
            if (requestDto.getSpecialOffers() != null) {
                plan.setSpecialOffers(requestDto.getSpecialOffers());
            }
            
            plan = subscriptionPlanRepository.save(plan);
            log.info("Subscription plan updated successfully with id: {}", plan.getId());
            
            return mapToResponseDto(plan);
        } catch (Exception e) {
            log.error("Error updating subscription plan: {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }

    @Override
    public QuotationResponseDto submitQuotation(String userId, QuotationRequestDto requestDto) {
        try {
            log.info("Submitting quotation for user: {}", userId);
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AbstractException("User not found"));
            
            SubscriptionPlan plan = subscriptionPlanRepository.findById(requestDto.getPlanId())
                    .orElseThrow(() -> new AbstractException("Subscription plan not found"));

            // Check if user already has a pending quotation
            Optional<PaymentQuotation> existingQuotation = paymentQuotationRepository
                    .findByOwnerIdAndStatus(userId, QuotationStatus.PENDING);
            
            if (existingQuotation.isPresent()) {
                throw new AbstractException("You already have a pending quotation. Please wait for approval.");
            }

            // Save file
            String filePath = saveQuotationFile(requestDto.getQuotationFile());

            PaymentQuotation quotation = PaymentQuotation.builder()
                    .owner(user)
                    .ownerId(userId)
                    .plan(plan)
                    .planId(plan.getId())
                    .filePath(filePath)
                    .status(QuotationStatus.PENDING)
                    .build();

            quotation = paymentQuotationRepository.save(quotation);

            return mapToQuotationResponseDto(quotation);
        } catch (Exception e) {
            log.error("Error submitting quotation: {}", e.getMessage());
            throw new AbstractException("Failed to submit quotation: " + e.getMessage());
        }
    }

    @Override
    public QuotationResponseDto getQuotationByOwnerId(String ownerId) {
        try {
            log.info("Fetching quotation for owner: {}", ownerId);
            PaymentQuotation quotation = paymentQuotationRepository.findByOwnerId(ownerId)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new AbstractException("Quotation not found"));
            return mapToQuotationResponseDto(quotation);
        } catch (Exception e) {
            log.error("Error fetching quotation: {}", e.getMessage());
            throw new AbstractException("Failed to fetch quotation: " + e.getMessage());
        }
    }

    private String saveQuotationFile(MultipartFile file) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    @Override
    public UserSubscriptionResponseDto createSubscription(String userId, CreateSubscriptionRequestDto requestDto) {
        try {
            log.info("Creating subscription for user: {} with plan: {} and {} users", 
                    userId, requestDto.getPlanId(), requestDto.getNumberOfUsers());
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AbstractException("User not found"));
            
            SubscriptionPlan plan = subscriptionPlanRepository.findById(requestDto.getPlanId())
                    .orElseThrow(() -> new AbstractException("Subscription plan not found"));
            
            // Check if user already has an active subscription
            Optional<UserSubscription> existingSubscription = userSubscriptionRepository.findByUserId(userId);
            if (existingSubscription.isPresent()) {
                throw new AbstractException("User already has an active subscription");
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
            
            return mapToUserSubscriptionResponseDto(subscription);
        } catch (Exception e) {
            log.error("Error creating subscription: {}", e.getMessage());
            throw new AbstractException("Failed to create subscription: " + e.getMessage());
        }
    }
    
    @Override
    public UserSubscriptionResponseDto getUserSubscription(String userId) {
        try {
            log.info("Fetching subscription for user: {}", userId);
            UserSubscription subscription = userSubscriptionRepository.findByUserId(userId)
                    .orElseThrow(() -> new AbstractException("Subscription not found"));
            return mapToUserSubscriptionResponseDto(subscription);
        } catch (Exception e) {
            log.error("Error fetching subscription: {}", e.getMessage());
            throw new AbstractException("Failed to fetch subscription: " + e.getMessage());
        }
    }
    
    @Override
    public UserSubscriptionResponseDto updateUsedUsers(String subscriptionId, int usedUsers) {
        try {
            log.info("Updating used users for subscription: {} to {}", subscriptionId, usedUsers);
            UserSubscription subscription = userSubscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new AbstractException("Subscription not found"));
            
            if (usedUsers > subscription.getNumberOfUsers()) {
                throw new AbstractException("Used users cannot exceed total number of users");
            }
            
            subscription.setUsedUsers(usedUsers);
            subscription.setRemainingUsers(subscription.getNumberOfUsers() - usedUsers);
            
            subscription = userSubscriptionRepository.save(subscription);
            
            return mapToUserSubscriptionResponseDto(subscription);
        } catch (Exception e) {
            log.error("Error updating used users: {}", e.getMessage());
            throw new AbstractException("Failed to update used users: " + e.getMessage());
        }
    }
    
    /**
     * Calculate discount based on special offers for the given number of users
     */
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
    
    private SubscriptionPlanResponseDto mapToResponseDto(SubscriptionPlan plan) {
        return SubscriptionPlanResponseDto.builder()
                .id(plan.getId())
                .name(plan.getName())
                .pricePerUser(plan.getPricePerUser())
                .description(plan.getDescription())
                .billingFrequency(plan.getBillingFrequency())
                .specialOffers(plan.getSpecialOffers())
                .build();
    }
    
    private UserSubscriptionResponseDto mapToUserSubscriptionResponseDto(UserSubscription subscription) {
        return UserSubscriptionResponseDto.builder()
                .id(subscription.getId())
                .userId(subscription.getUserId())
                .planId(subscription.getPlanId())
                .planName(subscription.getPlan() != null ? subscription.getPlan().getName() : null)
                .numberOfUsers(subscription.getNumberOfUsers())
                .usedUsers(subscription.getUsedUsers())
                .remainingUsers(subscription.getRemainingUsers())
                .total(subscription.getTotal())
                .discount(subscription.getDiscount())
                .finalPrice(subscription.getFinalPrice())
                .subscriptionDate(subscription.getSubscriptionDate())
                .expiryDate(subscription.getExpiryDate())
                .build();
    }

    private QuotationResponseDto mapToQuotationResponseDto(PaymentQuotation quotation) {
        return QuotationResponseDto.builder()
                .id(quotation.getId())
                .ownerId(quotation.getOwnerId())
                .planId(quotation.getPlanId())
                .planName(quotation.getPlan() != null ? quotation.getPlan().getName() : null)
                .filePath(quotation.getFilePath())
                .status(quotation.getStatus())
                .submissionDate(quotation.getSubmissionDate())
                .build();
    }
}
