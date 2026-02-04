package com.rawneeded.service.impl;

import com.rawneeded.dto.specialoffer.CreateSpecialOfferRequestDto;
import com.rawneeded.dto.specialoffer.SpecialOfferResponseDto;
import com.rawneeded.enumeration.PlanFeatures;
import com.rawneeded.enumeration.Role;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.jwt.JwtTokenProvider;
import com.rawneeded.model.Product;
import com.rawneeded.model.SupplierSpecialOffer;
import com.rawneeded.model.User;
import com.rawneeded.model.UserSubscription;
import com.rawneeded.repository.ProductRepository;
import com.rawneeded.repository.SupplierSpecialOfferRepository;
import com.rawneeded.repository.UserRepository;
import com.rawneeded.repository.UserSubscriptionRepository;
import com.rawneeded.service.ISpecialOfferService;
import com.rawneeded.util.MessagesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class SpecialOfferServiceImpl implements ISpecialOfferService {

    private final SupplierSpecialOfferRepository offerRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final UserSubscriptionRepository subscriptionRepository;
    private final JwtTokenProvider tokenProvider;
    private final MessagesUtil messagesUtil;

    @Override
    public SpecialOfferResponseDto createOffer(CreateSpecialOfferRequestDto request) {
        try {
            String token = messagesUtil.getAuthToken();
            String userId = tokenProvider.getOwnerIdFromToken(token);
            Role role = tokenProvider.getRoleFromToken(token);

            // Check if user is supplier
            if (role != Role.SUPPLIER_OWNER) {
                throw new AbstractException(messagesUtil.getMessage("UNAUTHORIZED"));
            }

            // Check if user has SUPPLIER_SPECIAL_OFFERS feature
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_NOT_FOUND")));
            
            Optional<UserSubscription> subscriptionOpt = subscriptionRepository.findFirstByUserId(userId);
            if (subscriptionOpt.isEmpty() || subscriptionOpt.get().getSelectedFeatures() == null ||
                    !subscriptionOpt.get().getSelectedFeatures().contains(PlanFeatures.SUPPLIER_SPECIAL_OFFERS)) {
                throw new AbstractException(messagesUtil.getMessage("FEATURE_NOT_AVAILABLE"));
            }

            // Validate product belongs to supplier
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("PRODUCT_NOT_FOUND")));

            if (!product.getSupplier().getId().equals(userId)) {
                throw new AbstractException(messagesUtil.getMessage("UNAUTHORIZED"));
            }

            // Validate dates
            if (request.getEndDate().isBefore(request.getStartDate())) {
                throw new AbstractException("End date must be after start date");
            }

            if (request.getEndDate().isBefore(LocalDateTime.now())) {
                throw new AbstractException("End date must be in the future");
            }

            // Validate discount percentage
            if (request.getDiscountPercentage() <= 0 || request.getDiscountPercentage() > 100) {
                throw new AbstractException("Discount percentage must be between 1 and 100");
            }

            SupplierSpecialOffer offer = SupplierSpecialOffer.builder()
                    .supplier(user)
                    .supplierId(userId)
                    .supplierName(user.getName())
                    .supplierOrganizationName(user.getOrganizationName())
                    .product(product)
                    .productId(product.getId())
                    .productName(product.getName())
                    .productImage(product.getImage())
                    .discountPercentage(request.getDiscountPercentage())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            offer = offerRepository.save(offer);
            return toResponseDto(offer);

        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating special offer: {}", e.getMessage(), e);
            throw new AbstractException("Failed to create special offer: " + e.getMessage());
        }
    }

    @Override
    public Page<SpecialOfferResponseDto> getMyOffers(Pageable pageable) {
        try {
            String token = messagesUtil.getAuthToken();
            String userId = tokenProvider.getOwnerIdFromToken(token);

            Page<SupplierSpecialOffer> offers = offerRepository.findBySupplierIdAndActiveTrueOrderByCreatedAtDesc(userId, pageable);
            return offers.map(this::toResponseDto);
        } catch (Exception e) {
            log.error("Error fetching my offers: {}", e.getMessage(), e);
            throw new AbstractException("Failed to fetch offers");
        }
    }

    @Override
    public Page<SpecialOfferResponseDto> getAllActiveOffers(Pageable pageable) {
        try {
            String token = messagesUtil.getAuthToken();
            Role role = tokenProvider.getRoleFromToken(token);
            
            // For customers, check if they have CUSTOMER_VIEW_SUPPLIER_OFFERS feature
            if (role == Role.CUSTOMER_OWNER || role == Role.CUSTOMER_STAFF) {
                String userId = tokenProvider.getOwnerIdFromToken(token);
                Optional<UserSubscription> subscriptionOpt = subscriptionRepository.findFirstByUserId(userId);
                if (subscriptionOpt.isEmpty() || subscriptionOpt.get().getSelectedFeatures() == null ||
                        !subscriptionOpt.get().getSelectedFeatures().contains(PlanFeatures.CUSTOMER_VIEW_SUPPLIER_OFFERS)) {
                    throw new AbstractException(messagesUtil.getMessage("FEATURE_NOT_AVAILABLE"));
                }
            }
            
            LocalDateTime now = LocalDateTime.now();
            Page<SupplierSpecialOffer> offers = offerRepository.findByActiveTrueAndStartDateBeforeAndEndDateAfterOrderByCreatedAtDesc(
                    now, now, pageable);
            return offers.map(this::toResponseDto);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching active offers: {}", e.getMessage(), e);
            throw new AbstractException("Failed to fetch active offers");
        }
    }

    @Override
    public SpecialOfferResponseDto updateOffer(String offerId, CreateSpecialOfferRequestDto request) {
        try {
            String token = messagesUtil.getAuthToken();
            String userId = tokenProvider.getOwnerIdFromToken(token);

            SupplierSpecialOffer offer = offerRepository.findById(offerId)
                    .orElseThrow(() -> new AbstractException("Offer not found"));

            if (!offer.getSupplierId().equals(userId)) {
                throw new AbstractException(messagesUtil.getMessage("UNAUTHORIZED"));
            }

            // Validate dates
            if (request.getEndDate().isBefore(request.getStartDate())) {
                throw new AbstractException("End date must be after start date");
            }

            // Validate discount percentage
            if (request.getDiscountPercentage() <= 0 || request.getDiscountPercentage() > 100) {
                throw new AbstractException("Discount percentage must be between 1 and 100");
            }

            offer.setDiscountPercentage(request.getDiscountPercentage());
            offer.setStartDate(request.getStartDate());
            offer.setEndDate(request.getEndDate());
            offer.setUpdatedAt(LocalDateTime.now());

            offer = offerRepository.save(offer);
            return toResponseDto(offer);

        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating offer: {}", e.getMessage(), e);
            throw new AbstractException("Failed to update offer");
        }
    }

    @Override
    public void deleteOffer(String offerId) {
        try {
            String token = messagesUtil.getAuthToken();
            String userId = tokenProvider.getOwnerIdFromToken(token);

            SupplierSpecialOffer offer = offerRepository.findById(offerId)
                    .orElseThrow(() -> new AbstractException("Offer not found"));

            if (!offer.getSupplierId().equals(userId)) {
                throw new AbstractException(messagesUtil.getMessage("UNAUTHORIZED"));
            }

            offer.setActive(false);
            offer.setUpdatedAt(LocalDateTime.now());
            offerRepository.save(offer);

        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting offer: {}", e.getMessage(), e);
            throw new AbstractException("Failed to delete offer");
        }
    }

    private SpecialOfferResponseDto toResponseDto(SupplierSpecialOffer offer) {
        return SpecialOfferResponseDto.builder()
                .id(offer.getId())
                .supplierId(offer.getSupplierId())
                .supplierName(offer.getSupplierName())
                .supplierOrganizationName(offer.getSupplierOrganizationName())
                .productId(offer.getProductId())
                .productName(offer.getProductName())
                .productImage(offer.getProductImage())
                .discountPercentage(offer.getDiscountPercentage())
                .startDate(offer.getStartDate())
                .endDate(offer.getEndDate())
                .active(offer.isActive())
                .createdAt(offer.getCreatedAt())
                .updatedAt(offer.getUpdatedAt())
                .build();
    }
}
