package com.rawneeded.service.impl;

import com.rawneeded.dto.staff.CreateStaffDto;
import com.rawneeded.dto.user.UserResponseDto;
import com.rawneeded.enumeration.AccountStatus;
import com.rawneeded.enumeration.Role;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.error.exceptions.PlanLimitExceededException;
import com.rawneeded.mapper.UserMapper;
import com.rawneeded.model.SubscriptionPlan;
import com.rawneeded.model.User;
import com.rawneeded.repository.UserRepository;
import com.rawneeded.service.IStaffService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class StaffServiceImpl implements IStaffService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final MongoTemplate mongoTemplate;

    @Override
    public UserResponseDto createStaff(CreateStaffDto dto) {
        try {
            log.info("Creating staff member: {}", dto.getEmail());

            // Validate owner exists
            User owner = userRepository.findById(dto.getOwnerId())
                    .orElseThrow(() -> new AbstractException("Owner not found"));

            // Check if owner has an active subscription plan
            SubscriptionPlan plan = owner.getSubscriptionPlan();
            if (plan == null) {
                throw new AbstractException("Owner does not have an active subscription plan");
            }

            // Count current staff members for this owner
            Query query = new Query();
            query.addCriteria(Criteria.where("ownerId").is(dto.getOwnerId()));
            long currentStaffCount = mongoTemplate.count(query, User.class);

            // Check if adding this staff would exceed the plan limit
            if (currentStaffCount >= plan.getUserLimit()) {
                throw new PlanLimitExceededException(
                        String.format("Cannot add more staff. Current plan allows %d users and you have reached the limit.", 
                                plan.getUserLimit()));
            }

            // Validate role is a staff role
            if (dto.getRole() != Role.SUPPLIER_STAFF && dto.getRole() != Role.CUSTOMER_STAFF) {
                throw new AbstractException("Invalid role for staff member");
            }

            // Create staff user
            User staff = User.builder()
                    .name(dto.getName())
                    .email(dto.getEmail())
                    .phoneNumber(dto.getPhoneNumber())
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .role(dto.getRole())
                    .ownerId(dto.getOwnerId())
                    .accountStatus(AccountStatus.ACTIVE)
                    .build();

            // Assign allowed screens (list of strings)
            if (dto.getAllowedScreenIds() != null && !dto.getAllowedScreenIds().isEmpty()) {
                staff.setAllowedScreens(dto.getAllowedScreenIds());
            }

            staff = userRepository.save(staff);

            log.info("Staff member created successfully: {}", staff.getId());
            return userMapper.toResponseDto(staff);
        } catch (PlanLimitExceededException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating staff member: {}", e.getMessage());
            throw new AbstractException("Failed to create staff member: " + e.getMessage());
        }
    }
}
