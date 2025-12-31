package com.rawneeded.service.impl;

import com.rawneeded.dto.subscription.QuotationResponseDto;
import com.rawneeded.enumeration.AccountStatus;
import com.rawneeded.enumeration.QuotationStatus;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.model.PaymentQuotation;
import com.rawneeded.model.User;
import com.rawneeded.repository.PaymentQuotationRepository;
import com.rawneeded.repository.UserRepository;
import com.rawneeded.service.IAdminService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class AdminServiceImpl implements IAdminService {

    private final PaymentQuotationRepository paymentQuotationRepository;
    private final UserRepository userRepository;

    @Override
    public List<QuotationResponseDto> getAllPendingQuotations() {
        try {
            log.info("Fetching all pending quotations");
            List<PaymentQuotation> quotations = paymentQuotationRepository.findAll()
                    .stream()
                    .filter(q -> q.getStatus() == QuotationStatus.PENDING)
                    .collect(Collectors.toList());
            
            return quotations.stream()
                    .map(this::mapToQuotationResponseDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching pending quotations: {}", e.getMessage());
            throw new AbstractException("Failed to fetch pending quotations: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public QuotationResponseDto approveQuotation(String quotationId) {
        try {
            log.info("Approving quotation: {}", quotationId);
            
            PaymentQuotation quotation = paymentQuotationRepository.findById(quotationId)
                    .orElseThrow(() -> new AbstractException("Quotation not found"));

            if (quotation.getStatus() != QuotationStatus.PENDING) {
                throw new AbstractException("Quotation is not in PENDING status");
            }

            // Update quotation status
            quotation.setStatus(QuotationStatus.APPROVED);
            quotation = paymentQuotationRepository.save(quotation);

            // Get the owner
            User owner = userRepository.findById(quotation.getOwnerId())
                    .orElseThrow(() -> new AbstractException("Owner not found"));

            // Update owner's account status to ACTIVE
            owner.setAccountStatus(AccountStatus.ACTIVE);

            // Link owner to the selected subscription plan
            if (quotation.getPlan() != null) {
                owner.setSubscriptionPlan(quotation.getPlan());
            }

            userRepository.save(owner);

            log.info("Quotation approved successfully. Owner {} activated with plan {}", 
                    owner.getId(), quotation.getPlanId());

            return mapToQuotationResponseDto(quotation);
        } catch (Exception e) {
            log.error("Error approving quotation: {}", e.getMessage());
            throw new AbstractException("Failed to approve quotation: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public QuotationResponseDto rejectQuotation(String quotationId, String reason) {
        try {
            log.info("Rejecting quotation: {} with reason: {}", quotationId, reason);
            
            PaymentQuotation quotation = paymentQuotationRepository.findById(quotationId)
                    .orElseThrow(() -> new AbstractException("Quotation not found"));

            if (quotation.getStatus() != QuotationStatus.PENDING) {
                throw new AbstractException("Quotation is not in PENDING status");
            }

            quotation.setStatus(QuotationStatus.REJECTED);
            quotation = paymentQuotationRepository.save(quotation);

            return mapToQuotationResponseDto(quotation);
        } catch (Exception e) {
            log.error("Error rejecting quotation: {}", e.getMessage());
            throw new AbstractException("Failed to reject quotation: " + e.getMessage());
        }
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
