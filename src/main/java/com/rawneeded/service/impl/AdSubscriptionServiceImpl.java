package com.rawneeded.service.impl;

import com.rawneeded.dto.MailDto;
import com.rawneeded.dto.advertisement.AdSubscriptionResponseDto;
import com.rawneeded.dto.advertisement.CreateAdSubscriptionRequestDto;
import com.rawneeded.enumeration.NotificationType;
import com.rawneeded.enumeration.Role;
import com.rawneeded.enumeration.TemplateName;
import com.rawneeded.enumeration.UserSubscriptionStatus;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.jwt.JwtTokenProvider;
import com.rawneeded.model.AdPackage;
import com.rawneeded.model.AdSpecialOffer;
import com.rawneeded.model.AdSubscription;
import com.rawneeded.model.User;
import com.rawneeded.repository.AdPackageRepository;
import com.rawneeded.repository.AdSubscriptionRepository;
import com.rawneeded.repository.UserRepository;
import com.rawneeded.service.IAdSubscriptionService;
import com.rawneeded.service.INotificationService;
import com.rawneeded.util.MessagesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.rawneeded.enumeration.UserSubscriptionStatus.APPROVED;
import static com.rawneeded.enumeration.UserSubscriptionStatus.PENDING;

@Slf4j
@Service
@AllArgsConstructor
public class AdSubscriptionServiceImpl implements IAdSubscriptionService {

    private final AdSubscriptionRepository adSubscriptionRepository;
    private final AdPackageRepository adPackageRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final MessagesUtil messagesUtil;
    private final INotificationService notificationService;
    private final NotificationService emailService;

    @Override
    public AdSubscriptionResponseDto createSubscription(CreateAdSubscriptionRequestDto dto) {
        String token = messagesUtil.getAuthToken();
        String supplierId = jwtTokenProvider.getOwnerIdFromToken(token);
        Role role = jwtTokenProvider.getRoleFromToken(token);
        if (role != Role.SUPPLIER_OWNER && role != Role.SUPPLIER_STAFF) {
            throw new AbstractException(messagesUtil.getMessage("FORBIDDEN"));
        }

        User supplier = userRepository.findById(supplierId)
                .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_NOT_FOUND")));
        AdPackage adPackage = adPackageRepository.findById(dto.getAdPackageId())
                .orElseThrow(() -> new AbstractException("Ad package not found"));
        if (!adPackage.isActive()) {
            throw new AbstractException("Ad package is not active");
        }
        int numberOfAds = dto.getNumberOfAds() >= 1 ? dto.getNumberOfAds() : 1;
        boolean featured = dto.isFeatured();
        BigDecimal basePrice = adPackage.getPricePerAd().multiply(BigDecimal.valueOf(numberOfAds));
        BigDecimal featuredPrice = featured && adPackage.getFeaturedPrice() != null ? adPackage.getFeaturedPrice() : BigDecimal.ZERO;
        BigDecimal totalPrice = basePrice.add(featuredPrice);
        
        // Calculate discount based on special offers
        BigDecimal discount = calculateDiscount(adPackage, numberOfAds, totalPrice);
        BigDecimal finalPrice = totalPrice.subtract(discount);

        LocalDateTime now = LocalDateTime.now();
        AdSubscription sub = AdSubscription.builder()
                .supplier(supplier)
                .supplierId(supplierId)
                .adPackage(adPackage)
                .adPackageId(adPackage.getId())
                .status(PENDING)
                .paymentProofPath(dto.getPaymentProofPath())
                .requestedAt(now)
                .packageNameAr(adPackage.getNameAr())
                .packageNameEn(adPackage.getNameEn())
                .numberOfDays(adPackage.getNumberOfDays())
                .pricePerAd(adPackage.getPricePerAd())
                .numberOfAds(numberOfAds)
                .featured(featured)
                .totalPrice(totalPrice)
                .discount(discount)
                .finalPrice(finalPrice)
                .remainingAds(0)
                .build();
        sub = adSubscriptionRepository.save(sub);
        log.info("Ad subscription created: {} for supplier: {}", sub.getId(), supplierId);
        String userName = supplier.getName() != null ? supplier.getName() : supplierId;
        notifyAdminsOfPendingAdSubscription(sub.getId(), userName);
        return toDto(sub);
    }

    private void notifyAdminsOfPendingAdSubscription(String entityId, String userName) {
        try {
            List<User> admins = userRepository.findAllByRole(Role.SUPER_ADMIN);
            String titleKey = "NOTIFICATION_PENDING_AD_SUBSCRIPTION_TITLE";
            String messageKey = "NOTIFICATION_PENDING_AD_SUBSCRIPTION_MESSAGE";
            String subject = messagesUtil.getMessage(titleKey);
            String description = messagesUtil.getMessage(messageKey);
            for (User admin : admins) {
                notificationService.sendNotificationToUser(
                        admin.getId(),
                        NotificationType.GENERAL,
                        titleKey,
                        messageKey,
                        entityId,
                        "AD_SUBSCRIPTION",
                        userName != null ? userName : ""
                );
                if (admin.getEmail() != null && !admin.getEmail().isEmpty()) {
                    try {
                        emailService.sendEmail(MailDto.builder()
                                .toEmail(admin.getEmail())
                                .subject(messagesUtil.getMessage("EMAIL_SUBJECT_PENDING_AD_SUBSCRIPTION"))
                                .templateName(TemplateName.COMPLAINT_CREATED_ADMIN)
                                .model(Map.of(
                                        "userName", userName != null ? userName : "",
                                        "subject", subject != null ? subject : "",
                                        "description", description != null ? description : ""
                                ))
                                .build());
                    } catch (Exception e) {
                        log.error("Failed to send pending ad-subscription email to admin: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error sending notifications/emails to admins: {}", e.getMessage());
        }
    }

    @Override
    public List<AdSubscriptionResponseDto> getMySubscriptions() {
        String token = messagesUtil.getAuthToken();
        String supplierId = jwtTokenProvider.getOwnerIdFromToken(token);
        List<AdSubscription> list = adSubscriptionRepository.findBySupplierIdOrderByRequestedAtDesc(supplierId);
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public Page<AdSubscriptionResponseDto> getPendingSubscriptions(Pageable pageable) {
        return adSubscriptionRepository.findByStatus(pageable, PENDING).map(this::toDto);
    }

    @Override
    public Page<AdSubscriptionResponseDto> getApprovedSubscriptions(Pageable pageable) {
        return adSubscriptionRepository.findByStatus(pageable, APPROVED).map(this::toDto);
    }

    @Override
    public AdSubscriptionResponseDto approve(String subscriptionId) {
        AdSubscription sub = adSubscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new AbstractException("Ad subscription not found"));
        if (sub.getStatus() != PENDING) {
            throw new AbstractException("Subscription is not pending");
        }

        LocalDateTime now = LocalDateTime.now();
        sub.setStatus(APPROVED);
        sub.setApprovedAt(now);
        sub.setStartDate(now);
        // No endDate on subscription: validity is per ad (each ad gets numberOfDays when created)
        sub.setRemainingAds(sub.getNumberOfAds());
        sub = adSubscriptionRepository.save(sub);
        log.info("Ad subscription approved: {}", subscriptionId);
        return toDto(sub);
    }

    @Override
    public AdSubscriptionResponseDto reject(String subscriptionId) {
        AdSubscription sub = adSubscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new AbstractException("Ad subscription not found"));
        if (sub.getStatus() != PENDING) {
            throw new AbstractException("Subscription is not pending");
        }
        sub.setStatus(UserSubscriptionStatus.REJECTED);
        sub = adSubscriptionRepository.save(sub);
        log.info("Ad subscription rejected: {}", subscriptionId);
        return toDto(sub);
    }

    @Override
    public boolean hasActiveSubscription(String supplierId) {
        return adSubscriptionRepository
                .findFirstBySupplierIdAndStatusAndRemainingAdsGreaterThanOrderByApprovedAtDesc(
                        supplierId, APPROVED, 0)
                .isPresent();
    }

    @Override
    public void consumeOneAd(String supplierId) {
        adSubscriptionRepository
                .findFirstBySupplierIdAndStatusAndRemainingAdsGreaterThanOrderByApprovedAtDesc(
                        supplierId, APPROVED, 0)
                .ifPresent(sub -> {
                    sub.setRemainingAds(Math.max(0, sub.getRemainingAds() - 1));
                    adSubscriptionRepository.save(sub);
                });
    }

    private AdSubscriptionResponseDto toDto(AdSubscription s) {
        String supplierName = null;
        String supplierOrganizationName = null;
        String supplierImage = null;
        if (s.getSupplier() != null) {
            supplierName = s.getSupplier().getName();
            supplierOrganizationName = s.getSupplier().getOrganizationName();
            supplierImage = s.getSupplier().getProfileImage();
        } else if (s.getSupplierId() != null) {
            var userOpt = userRepository.findById(s.getSupplierId());
            if (userOpt.isPresent()) {
                User u = userOpt.get();
                supplierName = u.getName();
                supplierOrganizationName = u.getOrganizationName();
                supplierImage = u.getProfileImage();
            }
        }
        return AdSubscriptionResponseDto.builder()
                .id(s.getId())
                .supplierId(s.getSupplierId())
                .supplierName(supplierName)
                .supplierOrganizationName(supplierOrganizationName)
                .supplierImage(supplierImage)
                .adPackageId(s.getAdPackageId())
                .status(s.getStatus())
                .paymentProofPath(s.getPaymentProofPath())
                .requestedAt(s.getRequestedAt())
                .approvedAt(s.getApprovedAt())
                .startDate(s.getStartDate())
                .endDate(s.getEndDate())
                .packageNameAr(s.getPackageNameAr())
                .packageNameEn(s.getPackageNameEn())
                .numberOfDays(s.getNumberOfDays())
                .pricePerAd(s.getPricePerAd() != null ? s.getPricePerAd() : s.getPrice())
                .numberOfAds(s.getNumberOfAds())
                .featured(s.isFeatured())
                .totalPrice(s.getTotalPrice())
                .discount(s.getDiscount() != null ? s.getDiscount() : BigDecimal.ZERO)
                .finalPrice(s.getFinalPrice() != null ? s.getFinalPrice() : (s.getTotalPrice() != null ? s.getTotalPrice() : BigDecimal.ZERO))
                .remainingAds(s.getRemainingAds())
                .build();
    }

    /**
     * Calculate discount based on special offers in the ad package
     * Similar to calculateDiscount for subscription plans but based on number of ads
     */
    private BigDecimal calculateDiscount(AdPackage adPackage, int numberOfAds, BigDecimal totalPrice) {
        if (adPackage.getSpecialOffers() == null || adPackage.getSpecialOffers().isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Find the best applicable offer (highest discount for the given ad count)
        AdSpecialOffer bestOffer = adPackage.getSpecialOffers().stream()
                .filter(offer -> numberOfAds >= offer.getMinAdCount())
                .max(java.util.Comparator.comparing(AdSpecialOffer::getDiscountPercentage))
                .orElse(null);

        if (bestOffer != null) {
            double discountAmount = totalPrice.doubleValue() * (bestOffer.getDiscountPercentage() / 100.0);
            return BigDecimal.valueOf(discountAmount).setScale(2, java.math.RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO;
    }
}
