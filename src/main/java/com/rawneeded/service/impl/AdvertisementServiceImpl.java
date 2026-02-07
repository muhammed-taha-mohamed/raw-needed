package com.rawneeded.service.impl;

import com.rawneeded.dto.advertisement.AdvertisementResponseDto;
import com.rawneeded.dto.advertisement.CreateAdvertisementRequestDto;
import com.rawneeded.dto.advertisement.UpdateAdvertisementRequestDto;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.enumeration.Role;
import com.rawneeded.jwt.JwtTokenProvider;
import com.rawneeded.model.AdSubscription;
import com.rawneeded.model.Advertisement;
import com.rawneeded.model.AdPackage;
import com.rawneeded.model.User;
import com.rawneeded.repository.AdPackageRepository;
import com.rawneeded.repository.AdSubscriptionRepository;
import com.rawneeded.repository.AdvertisementRepository;
import com.rawneeded.repository.UserRepository;
import com.rawneeded.service.IAdSubscriptionService;
import com.rawneeded.service.IAdvertisementService;
import com.rawneeded.util.MessagesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.rawneeded.enumeration.UserSubscriptionStatus.APPROVED;

@Slf4j
@Service
@AllArgsConstructor
public class AdvertisementServiceImpl implements IAdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final UserRepository userRepository;
    private final AdPackageRepository adPackageRepository;
    private final AdSubscriptionRepository adSubscriptionRepository;
    private final IAdSubscriptionService adSubscriptionService;
    private final MessagesUtil messagesUtil;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public AdvertisementResponseDto createAdvertisement(CreateAdvertisementRequestDto requestDto) {
        try {
            String token = messagesUtil.getAuthToken();
            String userId = jwtTokenProvider.getOwnerIdFromToken(token);
            Role role = jwtTokenProvider.getRoleFromToken(token);
            log.info("Creating advertisement for user: {}", userId);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_NOT_FOUND")));

            AdPackage adPackage = adPackageRepository.findById(requestDto.getAdPackageId())
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("ADVERTISEMENT_PACKAGE_INACTIVE")));
            if (!adPackage.isActive()) {
                throw new AbstractException(messagesUtil.getMessage("ADVERTISEMENT_PACKAGE_INACTIVE"));
            }

            // Supplier must have an approved ad subscription before adding an ad
            boolean isFeatured = requestDto.isFeatured();
            if (role == Role.SUPPLIER_OWNER || role == Role.SUPPLIER_STAFF) {
                LocalDateTime now = LocalDateTime.now();
                AdSubscription activeSub = adSubscriptionRepository
                        .findFirstBySupplierIdAndStatusAndRemainingAdsGreaterThanOrderByApprovedAtDesc(
                                userId, APPROVED, 0)
                        .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("ADVERTISEMENT_SUBSCRIPTION_REQUIRED")));
                // Use featured from subscription instead of request
                isFeatured = activeSub.isFeatured();
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endDate = now.plusDays(adPackage.getNumberOfDays());

            Advertisement advertisement = Advertisement.builder()
                    .user(user)
                    .userId(userId)
                    .image(requestDto.getImage())
                    .text(requestDto.getText())
                    .startDate(now)
                    .endDate(endDate)
                    .featured(isFeatured)
                    .createdAt(now)
                    .updatedAt(now)
                    .active(true)
                    .build();

            advertisement = advertisementRepository.save(advertisement);
            log.info("Advertisement created successfully with id: {}", advertisement.getId());

            if (role == Role.SUPPLIER_OWNER || role == Role.SUPPLIER_STAFF) {
                adSubscriptionService.consumeOneAd(userId);
            }

            return mapToResponseDto(advertisement);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating advertisement: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("ADVERTISEMENT_CREATE_FAIL"));
        }
    }

    @Override
    public AdvertisementResponseDto updateAdvertisement(String advertisementId, UpdateAdvertisementRequestDto requestDto) {
        try {
            String token = messagesUtil.getAuthToken();
            String userId = jwtTokenProvider.getOwnerIdFromToken(token);
            log.info("Updating advertisement: {} for user: {}", advertisementId, userId);

            Advertisement advertisement = advertisementRepository.findByIdAndUserId(advertisementId, userId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("ADVERTISEMENT_NOT_FOUND")));

            if (requestDto.getImage() != null && !requestDto.getImage().isEmpty()) {
                advertisement.setImage(requestDto.getImage());
            }

            if (requestDto.getText() != null && !requestDto.getText().isEmpty()) {
                advertisement.setText(requestDto.getText());
            }

            advertisement.setUpdatedAt(LocalDateTime.now());
            advertisement = advertisementRepository.save(advertisement);

            log.info("Advertisement updated successfully");
            return mapToResponseDto(advertisement);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating advertisement: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("ADVERTISEMENT_UPDATE_FAIL"));
        }
    }

    @Override
    public void deleteAdvertisement(String advertisementId) {
        try {
            String token = messagesUtil.getAuthToken();
            String userId = jwtTokenProvider.getOwnerIdFromToken(token);
            log.info("Deleting advertisement: {} for user: {}", advertisementId, userId);

            Advertisement advertisement = advertisementRepository.findByIdAndUserId(advertisementId, userId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("ADVERTISEMENT_NOT_FOUND")));

            advertisementRepository.delete(advertisement);
            log.info("Advertisement deleted successfully");
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting advertisement: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("ADVERTISEMENT_DELETE_FAIL"));
        }
    }

    @Override
    public List<AdvertisementResponseDto> getMyAdvertisements() {
        try {
            String token = messagesUtil.getAuthToken();
            String userId = jwtTokenProvider.getOwnerIdFromToken(token);
            log.info("Fetching advertisements for user: {}", userId);

            List<Advertisement> advertisements = advertisementRepository.findByUserIdOrderByCreatedAtDesc(userId);
            return advertisements.stream()
                    .map(this::mapToResponseDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching user advertisements: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("ADVERTISEMENT_FETCH_MY_FAIL"));
        }
    }

    @Override
    public Page<AdvertisementResponseDto> getAllAdvertisements(Pageable pageable) {
        try {
            log.info("Fetching all active non-expired advertisements (featured first)");
            LocalDateTime now = LocalDateTime.now();
            Sort sort = Sort.by(Sort.Direction.DESC, "featured").and(Sort.by(Sort.Direction.DESC, "createdAt"));
            Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
            Page<Advertisement> advertisements = advertisementRepository.findActiveAndNotExpired(now, sortedPageable);
            return advertisements.map(this::mapToResponseDto);
        } catch (Exception e) {
            log.error("Error fetching all advertisements: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("ADVERTISEMENT_FETCH_ALL_FAIL"));
        }
    }

    @Override
    public AdvertisementResponseDto getAdvertisementById(String advertisementId) {
        try {
            log.info("Fetching advertisement: {}", advertisementId);
            Advertisement advertisement = advertisementRepository.findById(advertisementId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("ADVERTISEMENT_NOT_FOUND")));
            return mapToResponseDto(advertisement);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching advertisement: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("ADVERTISEMENT_FETCH_FAIL"));
        }
    }

    private AdvertisementResponseDto mapToResponseDto(Advertisement advertisement) {
        return AdvertisementResponseDto.builder()
                .id(advertisement.getId())
                .userId(advertisement.getUserId())
                .image(advertisement.getImage())
                .text(advertisement.getText())
                .startDate(advertisement.getStartDate())
                .endDate(advertisement.getEndDate())
                .featured(advertisement.isFeatured())
                .createdAt(advertisement.getCreatedAt())
                .updatedAt(advertisement.getUpdatedAt())
                .active(advertisement.isActive())
                .build();
    }
}
