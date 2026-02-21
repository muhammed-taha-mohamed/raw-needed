package com.rawneeded.service;

import com.rawneeded.dto.advertisement.AdvertisementResponseDto;
import com.rawneeded.dto.advertisement.AdvertisementViewStatsDto;
import com.rawneeded.dto.advertisement.CreateAdvertisementRequestDto;
import com.rawneeded.dto.advertisement.UpdateAdvertisementRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IAdvertisementService {
    AdvertisementResponseDto createAdvertisement(CreateAdvertisementRequestDto requestDto);
    AdvertisementResponseDto updateAdvertisement(String advertisementId, UpdateAdvertisementRequestDto requestDto);
    void deleteAdvertisement(String advertisementId);
    List<AdvertisementResponseDto> getMyAdvertisements();
    Page<AdvertisementResponseDto> getAllAdvertisements(Pageable pageable);
    AdvertisementResponseDto getAdvertisementById(String advertisementId);
    void recordView(String advertisementId);
    List<AdvertisementViewStatsDto> getViewStats(String advertisementId);
}
