package com.rawneeded.service.impl;

import com.rawneeded.dto.subscription.QuotationRequestDto;
import com.rawneeded.dto.subscription.QuotationResponseDto;
import com.rawneeded.dto.subscription.SubscriptionPlanResponseDto;
import com.rawneeded.enumeration.QuotationStatus;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.model.PaymentQuotation;
import com.rawneeded.model.SubscriptionPlan;
import com.rawneeded.model.User;
import com.rawneeded.repository.PaymentQuotationRepository;
import com.rawneeded.repository.SubscriptionPlanRepository;
import com.rawneeded.repository.UserRepository;
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

    private SubscriptionPlanResponseDto mapToResponseDto(SubscriptionPlan plan) {
        return SubscriptionPlanResponseDto.builder()
                .id(plan.getId())
                .name(plan.getName())
                .price(plan.getPrice())
                .userLimit(plan.getUserLimit())
                .description(plan.getDescription())
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
