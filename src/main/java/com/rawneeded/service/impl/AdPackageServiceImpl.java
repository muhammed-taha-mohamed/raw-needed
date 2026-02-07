package com.rawneeded.service.impl;

import com.rawneeded.dto.advertisement.AdPackageResponseDto;
import com.rawneeded.dto.advertisement.CreateAdPackageRequestDto;
import com.rawneeded.dto.advertisement.UpdateAdPackageRequestDto;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.model.AdPackage;
import com.rawneeded.repository.AdPackageRepository;
import com.rawneeded.service.IAdPackageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class AdPackageServiceImpl implements IAdPackageService {

    private final AdPackageRepository adPackageRepository;

    @Override
    public List<AdPackageResponseDto> getAllPackages() {
        return adPackageRepository.findAll().stream()
                .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AdPackageResponseDto> getActivePackagesForSuppliers() {
        return adPackageRepository.findByActiveTrueOrderBySortOrderAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public AdPackageResponseDto createPackage(CreateAdPackageRequestDto dto) {
        int order = dto.getSortOrder() != null ? dto.getSortOrder() : 0;
        AdPackage pkg = AdPackage.builder()
                .nameAr(dto.getNameAr())
                .nameEn(dto.getNameEn())
                .numberOfDays(dto.getNumberOfDays())
                .pricePerAd(dto.getPricePerAd())
                .featuredPrice(dto.getFeaturedPrice() != null ? dto.getFeaturedPrice() : BigDecimal.ZERO)
                .active(dto.getActive() != null ? dto.getActive() : true)
                .sortOrder(order)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        pkg = adPackageRepository.save(pkg);
        return toDto(pkg);
    }

    @Override
    public AdPackageResponseDto updatePackage(String packageId, UpdateAdPackageRequestDto dto) {
        AdPackage pkg = adPackageRepository.findById(packageId)
                .orElseThrow(() -> new AbstractException("Ad package not found"));
        if (dto.getNameAr() != null) pkg.setNameAr(dto.getNameAr());
        if (dto.getNameEn() != null) pkg.setNameEn(dto.getNameEn());
        if (dto.getNumberOfDays() != null) pkg.setNumberOfDays(dto.getNumberOfDays());
        if (dto.getPricePerAd() != null) pkg.setPricePerAd(dto.getPricePerAd());
        if (dto.getActive() != null) pkg.setActive(dto.getActive());
        if (dto.getSortOrder() != null) pkg.setSortOrder(dto.getSortOrder());
        pkg.setUpdatedAt(LocalDateTime.now());
        pkg = adPackageRepository.save(pkg);
        return toDto(pkg);
    }

    @Override
    public void deletePackage(String packageId) {
        if (!adPackageRepository.existsById(packageId)) {
            throw new AbstractException("Ad package not found");
        }
        adPackageRepository.deleteById(packageId);
    }

    public AdPackage findByIdOrThrow(String id) {
        return adPackageRepository.findById(id)
                .orElseThrow(() -> new AbstractException("Ad package not found"));
    }

    private AdPackageResponseDto toDto(AdPackage p) {
        BigDecimal pricePerAd = p.getPricePerAd();
        if (pricePerAd == null && p.getPrice() != null) pricePerAd = p.getPrice();
        return AdPackageResponseDto.builder()
                .id(p.getId())
                .nameAr(p.getNameAr())
                .nameEn(p.getNameEn())
                .numberOfDays(p.getNumberOfDays())
                .pricePerAd(pricePerAd != null ? pricePerAd : java.math.BigDecimal.ZERO)
                .featuredPrice(p.getFeaturedPrice() != null ? p.getFeaturedPrice() : java.math.BigDecimal.ZERO)
                .active(p.isActive())
                .sortOrder(p.getSortOrder())
                .build();
    }
}
