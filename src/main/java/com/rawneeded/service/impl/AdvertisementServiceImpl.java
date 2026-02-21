package com.rawneeded.service.impl;

import com.rawneeded.dto.advertisement.AdvertisementResponseDto;
import com.rawneeded.dto.advertisement.AdvertisementViewStatsDto;
import com.rawneeded.dto.advertisement.CreateAdvertisementRequestDto;
import com.rawneeded.dto.advertisement.UpdateAdvertisementRequestDto;
import com.rawneeded.model.AdvertisementView;
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
import com.rawneeded.repository.AdvertisementViewRepository;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.rawneeded.enumeration.UserSubscriptionStatus.APPROVED;

@Slf4j
@Service
@AllArgsConstructor
public class AdvertisementServiceImpl implements IAdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final AdvertisementViewRepository advertisementViewRepository;
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

            advertisement.setHidden(false);
            advertisement = advertisementRepository.save(advertisement);
            log.info("Advertisement created successfully with id: {}", advertisement.getId());

            if (role == Role.SUPPLIER_OWNER || role == Role.SUPPLIER_STAFF) {
                adSubscriptionService.consumeOneAd(userId);
            }

            return mapToResponseDtoWithViews(advertisement);
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
            return mapToResponseDtoWithViews(advertisement);
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
            log.info("Hiding advertisement: {} for user: {}", advertisementId, userId);

            Advertisement advertisement = advertisementRepository.findByIdAndUserId(advertisementId, userId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("ADVERTISEMENT_NOT_FOUND")));

            // Hide instead of delete
            advertisement.setHidden(true);
            advertisement.setActive(false);
            advertisement.setUpdatedAt(LocalDateTime.now());
            advertisementRepository.save(advertisement);
            log.info("Advertisement hidden successfully");
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error hiding advertisement: {}", e.getMessage(), e);
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
                    .map(this::mapToResponseDtoWithViews)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching user advertisements: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("ADVERTISEMENT_FETCH_MY_FAIL"));
        }
    }

    @Override
    public Page<AdvertisementResponseDto> getAllAdvertisements(Pageable pageable) {
        try {
            log.info("Fetching all active non-expired and non-hidden advertisements (featured first)");
            LocalDateTime now = LocalDateTime.now();
            Sort sort = Sort.by(Sort.Direction.DESC, "featured").and(Sort.by(Sort.Direction.DESC, "createdAt"));
            Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
            Page<Advertisement> advertisements = advertisementRepository.findActiveAndNotExpiredAndNotHidden(now, sortedPageable);
            return advertisements.map(this::mapToResponseDtoWithViews);
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
                .hidden(advertisement.isHidden())
                .build();
    }

    private AdvertisementResponseDto mapToResponseDtoWithViews(Advertisement advertisement) {
        LocalDateTime now = LocalDateTime.now();
        Long remainingDays = null;
        if (advertisement.getEndDate() != null) {
            if (advertisement.getEndDate().isAfter(now)) {
                // Calculate remaining days (can be 0 if less than 24 hours remaining)
                long hoursRemaining = Duration.between(now, advertisement.getEndDate()).toHours();
                remainingDays = hoursRemaining > 0 ? (hoursRemaining / 24) + (hoursRemaining % 24 > 0 ? 1 : 0) : 0L;
            } else {
                // Expired
                remainingDays = 0L;
            }
        }

        Long viewCount = advertisementViewRepository.countByAdvertisementId(advertisement.getId());

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
                .hidden(advertisement.isHidden())
                .remainingDays(remainingDays)
                .viewCount(viewCount)
                .build();
    }

    @Override
    public void recordView(String advertisementId) {
        try {
            String token = messagesUtil.getAuthToken();
            if (token == null || token.isEmpty()) {
                return; // User not logged in, don't record view
            }
            
            String viewerId = jwtTokenProvider.getOwnerIdFromToken(token);
            if (viewerId == null || viewerId.isEmpty()) {
                return; // Invalid token, don't record view
            }
            
            // Check if advertisement exists and is visible
            Advertisement advertisement = advertisementRepository.findById(advertisementId)
                    .orElse(null);
            
            if (advertisement == null || advertisement.isHidden() || !advertisement.isActive()) {
                log.debug("Skipping view recording: ad {} is null, hidden, or inactive", advertisementId);
                return; // Don't record views for hidden/inactive ads
            }
            
            // Check if already viewed by this user - only count once per user
            boolean alreadyViewed = advertisementViewRepository.existsByAdvertisementIdAndViewerId(advertisementId, viewerId);
            if (alreadyViewed) {
                log.debug("Skipping view recording: user {} already viewed ad {}", viewerId, advertisementId);
                return; // Already viewed, skip - one view per user
            }
            
            User viewer = userRepository.findById(viewerId).orElse(null);
            if (viewer == null) {
                log.debug("Skipping view recording: user {} not found", viewerId);
                return;
            }
            
            AdvertisementView view = AdvertisementView.builder()
                    .advertisement(advertisement)
                    .advertisementId(advertisementId)
                    .viewer(viewer)
                    .viewerId(viewerId)
                    .viewedAt(LocalDateTime.now())
                    .build();
            
            advertisementViewRepository.save(view);
            log.info("Recorded view for advertisement {} by user {} at {}", advertisementId, viewerId, LocalDateTime.now());
        } catch (Exception e) {
            log.error("Error recording advertisement view: {}", e.getMessage(), e);
            // Don't throw exception - view tracking is not critical
        }
    }

    @Override
    public List<AdvertisementViewStatsDto> getViewStats(String advertisementId) {
        try {
            String token = messagesUtil.getAuthToken();
            String userId = jwtTokenProvider.getOwnerIdFromToken(token);
            
            // Verify user owns the advertisement
            Advertisement advertisement = advertisementRepository.findByIdAndUserId(advertisementId, userId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("ADVERTISEMENT_NOT_FOUND")));
            
            List<AdvertisementView> views = advertisementViewRepository.findByAdvertisementIdOrderByViewedAtDesc(advertisementId);
            
            return views.stream()
                    .map(view -> {
                        User viewer = view.getViewer();
                        return AdvertisementViewStatsDto.builder()
                                .viewerId(view.getViewerId())
                                .viewerName(viewer != null ? (viewer.getName() != null ? viewer.getName() : viewer.getEmail()) : "Unknown")
                                .viewerEmail(viewer != null ? viewer.getEmail() : "Unknown")
                                .viewedAt(view.getViewedAt())
                                .build();
                    })
                    .collect(Collectors.toList());
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching view stats: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("ADVERTISEMENT_FETCH_STATS_FAIL"));
        }
    }
}
