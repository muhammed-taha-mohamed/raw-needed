package com.rawneeded.service;

import com.rawneeded.dto.advertisement.AdSettingsResponseDto;
import com.rawneeded.dto.advertisement.UpdateAdSettingsRequestDto;

public interface IAdSettingsService {
    AdSettingsResponseDto getSettings();
    AdSettingsResponseDto updateSettings(UpdateAdSettingsRequestDto dto);
}
