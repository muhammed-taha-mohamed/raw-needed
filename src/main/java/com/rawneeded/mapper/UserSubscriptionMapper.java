package com.rawneeded.mapper;

import com.rawneeded.dto.subscription.CalculatePriceRequestDto;
import com.rawneeded.dto.subscription.UserSubscriptionResponseDto;
import com.rawneeded.model.UserSubscription;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface UserSubscriptionMapper {
    UserSubscriptionResponseDto toResponseDto(UserSubscription userSubscription);

    default Page<UserSubscriptionResponseDto> toResponsePages(Page<UserSubscription> page) {
        return page.map(this::toResponseDto);
    }

    UserSubscription toEntity(CalculatePriceRequestDto userSubscriptionResponseDto);

}
