package com.rawneeded.service.impl;

import com.rawneeded.dto.advertisement.AdvertisementResponseDto;
import com.rawneeded.dto.advertisement.CreateAdvertisementRequestDto;
import com.rawneeded.dto.advertisement.UpdateAdvertisementRequestDto;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.jwt.JwtTokenProvider;
import com.rawneeded.model.Advertisement;
import com.rawneeded.model.User;
import com.rawneeded.model.UserSubscription;
import com.rawneeded.repository.AdvertisementRepository;
import com.rawneeded.repository.UserRepository;
import com.rawneeded.service.IAdvertisementService;
import com.rawneeded.util.MessagesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class AdvertisementServiceImpl implements IAdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final UserRepository userRepository;
    private final MessagesUtil messagesUtil;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public AdvertisementResponseDto createAdvertisement(CreateAdvertisementRequestDto requestDto) {
        try {
            String token = messagesUtil.getAuthToken();
            String userId = jwtTokenProvider.getOwnerIdFromToken(token);
            log.info("Creating advertisement for user: {}", userId);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_NOT_FOUND")));

            // Check if a user has a subscription with advertisements enabled
            UserSubscription subscription = user.getSubscription();
            if (subscription == null || subscription.getPlan() == null || !subscription.getPlan().isHasAdvertisements()) {
                throw new AbstractException(messagesUtil.getMessage("ADVERTISEMENT_PLAN_NOT_ALLOWED"));
            }

            Advertisement advertisement = Advertisement.builder()
                    .user(user)
                    .userId(userId)
                    .image(requestDto.getImage())
                    .text(requestDto.getText())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .active(true)
                    .build();

            advertisement = advertisementRepository.save(advertisement);
            log.info("Advertisement created successfully with id: {}", advertisement.getId());

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
            log.info("Fetching all active advertisements");
            Page<Advertisement> advertisements = advertisementRepository.findByActiveTrueOrderByCreatedAtDesc(pageable);
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
                .createdAt(advertisement.getCreatedAt())
                .updatedAt(advertisement.getUpdatedAt())
                .active(advertisement.isActive())
                .build();
    }
}
