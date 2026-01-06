package com.rawneeded.mapper;

import com.rawneeded.dto.product.CartDTO;
import com.rawneeded.dto.subscription.CreatePlanRequestDto;
import com.rawneeded.dto.subscription.SubscriptionPlanResponseDto;
import com.rawneeded.dto.subscription.UpdatePlanRequestDto;
import com.rawneeded.dto.user.UserRequestDto;
import com.rawneeded.model.Cart;
import com.rawneeded.model.SubscriptionPlan;
import com.rawneeded.model.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PlanMapper {
    SubscriptionPlanResponseDto toResponseDto (SubscriptionPlan plan);
    SubscriptionPlan toEntity (CreatePlanRequestDto dto);
    List<SubscriptionPlanResponseDto> toResponseDtoList (List<SubscriptionPlan> plans);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    void update(@MappingTarget SubscriptionPlan plan, UpdatePlanRequestDto requestDTO);

}
