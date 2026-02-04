package com.rawneeded.service.impl;

import com.rawneeded.dto.advertisement.AdSubscriptionResponseDto;
import com.rawneeded.dto.advertisement.CreateAdSubscriptionRequestDto;
import com.rawneeded.enumeration.Role;
import com.rawneeded.enumeration.UserSubscriptionStatus;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.jwt.JwtTokenProvider;
import com.rawneeded.model.AdPackage;
import com.rawneeded.model.AdSubscription;
import com.rawneeded.model.User;
import com.rawneeded.repository.AdPackageRepository;
import com.rawneeded.repository.AdSubscriptionRepository;
import com.rawneeded.repository.UserRepository;
import com.rawneeded.service.IAdSubscriptionService;
import com.rawneeded.util.MessagesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
        BigDecimal totalPrice = adPackage.getPricePerAd().multiply(BigDecimal.valueOf(numberOfAds));

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
                .totalPrice(totalPrice)
                .remainingAds(0)
                .build();
        sub = adSubscriptionRepository.save(sub);
        log.info("Ad subscription created: {} for supplier: {}", sub.getId(), supplierId);
        return toDto(sub);
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
        sub.setEndDate(now.plusDays(sub.getNumberOfDays()));
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
        LocalDateTime now = LocalDateTime.now();
        return adSubscriptionRepository
                .findFirstBySupplierIdAndStatusAndEndDateAfterAndRemainingAdsGreaterThanOrderByApprovedAtDesc(
                        supplierId, APPROVED, now, 0)
                .isPresent();
    }

    @Override
    public void consumeOneAd(String supplierId) {
        LocalDateTime now = LocalDateTime.now();
        adSubscriptionRepository
                .findFirstBySupplierIdAndStatusAndEndDateAfterAndRemainingAdsGreaterThanOrderByApprovedAtDesc(
                        supplierId, APPROVED, now, 0)
                .ifPresent(sub -> {
                    sub.setRemainingAds(Math.max(0, sub.getRemainingAds() - 1));
                    adSubscriptionRepository.save(sub);
                });
    }

    private AdSubscriptionResponseDto toDto(AdSubscription s) {
        return AdSubscriptionResponseDto.builder()
                .id(s.getId())
                .supplierId(s.getSupplierId())
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
                .totalPrice(s.getTotalPrice())
                .remainingAds(s.getRemainingAds())
                .build();
    }
}
