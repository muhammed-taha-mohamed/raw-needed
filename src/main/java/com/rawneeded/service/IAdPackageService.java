package com.rawneeded.service;

import com.rawneeded.dto.advertisement.AdPackageResponseDto;
import com.rawneeded.dto.advertisement.CreateAdPackageRequestDto;
import com.rawneeded.dto.advertisement.UpdateAdPackageRequestDto;

import java.util.List;

public interface IAdPackageService {
    List<AdPackageResponseDto> getAllPackages();
    List<AdPackageResponseDto> getActivePackagesForSuppliers();
    AdPackageResponseDto createPackage(CreateAdPackageRequestDto dto);
    AdPackageResponseDto updatePackage(String packageId, UpdateAdPackageRequestDto dto);
    void deletePackage(String packageId);
}
