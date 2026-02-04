package com.rawneeded.service;

import com.rawneeded.dto.specialoffer.CreateSpecialOfferRequestDto;
import com.rawneeded.dto.specialoffer.SpecialOfferResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ISpecialOfferService {
    SpecialOfferResponseDto createOffer(CreateSpecialOfferRequestDto request);
    Page<SpecialOfferResponseDto> getMyOffers(Pageable pageable);
    Page<SpecialOfferResponseDto> getAllActiveOffers(Pageable pageable);
    SpecialOfferResponseDto updateOffer(String offerId, CreateSpecialOfferRequestDto request);
    void deleteOffer(String offerId);
}
