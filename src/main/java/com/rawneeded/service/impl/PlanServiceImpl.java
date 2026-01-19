package com.rawneeded.service.impl;

import com.rawneeded.dto.subscription.CreatePlanRequestDto;
import com.rawneeded.dto.subscription.SubscriptionPlanResponseDto;
import com.rawneeded.dto.subscription.UpdatePlanRequestDto;
import com.rawneeded.enumeration.PlanType;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.mapper.PlanMapper;
import com.rawneeded.model.SubscriptionPlan;
import com.rawneeded.repository.SubscriptionPlanRepository;
import com.rawneeded.service.IPlanService;
import com.rawneeded.util.MessagesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class PlanServiceImpl implements IPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PlanMapper planMapper;
    private final MessagesUtil messagesUtil;

    @Override
    public List<SubscriptionPlanResponseDto> getAllPlans() {
        try {
            log.info("Fetching all subscription plans");
            List<SubscriptionPlan> plans = subscriptionPlanRepository.findAll();
            return planMapper.toResponseDtoList(plans);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching subscription plans: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("PLAN_FETCH_ALL_FAIL"));
        }
    }

    @Override
    public List<SubscriptionPlanResponseDto> getPlansByType(PlanType planType) {
        try {
            log.info("Fetching subscription plans by type: {}", planType);
            List<SubscriptionPlan> plans = subscriptionPlanRepository.findByPlanTypeAndActiveTrue(planType);
            return planMapper.toResponseDtoList(plans);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching subscription plans by type: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("PLAN_FETCH_BY_TYPE_FAIL"));
        }
    }

    @Override
    public SubscriptionPlanResponseDto getPlanById(String planId) {
        try {
            log.info("Fetching subscription plan with id: {}", planId);
            SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("PLAN_NOT_FOUND")));
            return planMapper.toResponseDto(plan);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching subscription plan: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("PLAN_FETCH_ONE_FAIL"));
        }
    }

    @Override
    public SubscriptionPlanResponseDto createPlan(CreatePlanRequestDto requestDto) {
        try {
            log.info("Creating new subscription plan: {}", requestDto.getName());

            // Check if plan with the same name already exists
            boolean nameExists = subscriptionPlanRepository.existsByNameIgnoreCase(requestDto.getName());
            if (nameExists)
                throw new AbstractException(messagesUtil.getMessage("PLAN_NAME_EXISTS"));

            SubscriptionPlan plan = planMapper.toEntity(requestDto);
            plan.setActive(true);
            if (requestDto.getExclusive() != null) {
                plan.setExclusive(requestDto.getExclusive());
            }
            if (requestDto.getHasAdvertisements() != null) {
                plan.setHasAdvertisements(requestDto.getHasAdvertisements());
            }
            plan = subscriptionPlanRepository.save(plan);
            log.info("Subscription plan created successfully with id: {}", plan.getId());

            return planMapper.toResponseDto(plan);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating subscription plan: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("PLAN_CREATE_FAIL"));
        }
    }

    @Override
    public SubscriptionPlanResponseDto updatePlan(String planId, UpdatePlanRequestDto requestDto) {
        try {
            log.info("Updating subscription plan with id: {}", planId);

            SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("PLAN_NOT_FOUND")));

            // Check if the name is being changed and if new name already exists
            if (requestDto.getName() != null && !requestDto.getName().equals(plan.getName())) {
                boolean nameExists = subscriptionPlanRepository.existsByNameIgnoreCaseAndIdNot(requestDto.getName(), planId);
                if (nameExists) {
                    throw new AbstractException(messagesUtil.getMessage("PLAN_NAME_EXISTS"));
                }
                plan.setName(requestDto.getName());
            }

            planMapper.update(plan, requestDto);

            plan = subscriptionPlanRepository.save(plan);
            log.info("Subscription plan updated successfully with id: {}", plan.getId());

            return planMapper.toResponseDto(plan);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating subscription plan: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("PLAN_UPDATE_FAIL"));
        }
    }

    @Override
    public void deletePlan(String planId) {
        try {
            log.info("Deleting subscription plan with id: {}", planId);
            SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("PLAN_NOT_FOUND")));
            subscriptionPlanRepository.delete(plan);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting subscription plan: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("PLAN_DELETE_FAIL"));
        }
    }

    @Override
    public SubscriptionPlanResponseDto activatePlan(String planId) {
        try {
            log.info("Activating subscription plan: {}", planId);
            SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("PLAN_NOT_FOUND")));
            plan.setActive(true);
            plan = subscriptionPlanRepository.save(plan);
            return planMapper.toResponseDto(plan);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error activating plan: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("PLAN_ACTIVATE_FAIL"));
        }
    }

    @Override
    public SubscriptionPlanResponseDto deactivatePlan(String planId) {
        try {
            log.info("Deactivating subscription plan: {}", planId);
            SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("PLAN_NOT_FOUND")));
            plan.setActive(false);
            plan = subscriptionPlanRepository.save(plan);
            return planMapper.toResponseDto(plan);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deactivating plan: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("PLAN_DEACTIVATE_FAIL"));
        }
    }


}



