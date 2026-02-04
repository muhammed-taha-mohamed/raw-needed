package com.rawneeded.service.impl;

import com.rawneeded.dto.advertisement.AdSettingsResponseDto;
import com.rawneeded.dto.advertisement.UpdateAdSettingsRequestDto;
import com.rawneeded.model.AdSettings;
import com.rawneeded.repository.AdSettingsRepository;
import com.rawneeded.service.IAdSettingsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class AdSettingsServiceImpl implements IAdSettingsService {

    private static final String SETTINGS_ID = "default";

    private final AdSettingsRepository adSettingsRepository;

    @Override
    public AdSettingsResponseDto getSettings() {
        AdSettings settings = adSettingsRepository.findById(SETTINGS_ID).orElse(null);
        if (settings == null) {
            return AdSettingsResponseDto.builder()
                    .featuredPrice(BigDecimal.ZERO)
                    .build();
        }
        return AdSettingsResponseDto.builder()
                .featuredPrice(settings.getFeaturedPrice() != null ? settings.getFeaturedPrice() : BigDecimal.ZERO)
                .build();
    }

    @Override
    public AdSettingsResponseDto updateSettings(UpdateAdSettingsRequestDto dto) {
        AdSettings settings = adSettingsRepository.findById(SETTINGS_ID).orElse(null);
        if (settings == null) {
            settings = AdSettings.builder()
                    .id(SETTINGS_ID)
                    .featuredPrice(dto.getFeaturedPrice() != null ? dto.getFeaturedPrice() : BigDecimal.ZERO)
                    .build();
        } else if (dto.getFeaturedPrice() != null) {
            settings.setFeaturedPrice(dto.getFeaturedPrice());
        }
        settings = adSettingsRepository.save(settings);
        return AdSettingsResponseDto.builder()
                .featuredPrice(settings.getFeaturedPrice() != null ? settings.getFeaturedPrice() : BigDecimal.ZERO)
                .build();
    }
}
