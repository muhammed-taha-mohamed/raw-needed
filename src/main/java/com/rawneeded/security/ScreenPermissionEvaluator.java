package com.rawneeded.security;

import com.rawneeded.enumeration.Role;
import com.rawneeded.jwt.JwtTokenProvider;
import com.rawneeded.model.User;
import com.rawneeded.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class ScreenPermissionEvaluator implements PermissionEvaluator {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        try {
            String token = extractTokenFromAuthentication(authentication);
            if (token == null || !jwtTokenProvider.validateToken(token)) {
                return false;
            }

            String userId = jwtTokenProvider.getIdFromToken(token);
            String roleStr = jwtTokenProvider.getRoleFromToken(token).toString();

            if (userId == null || roleStr == null) {
                return false;
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return false;
            }

            Role role = Role.valueOf(roleStr);

            // SYSTEM_ADMIN and OWNERs have full access
            if (role == Role.SUPER_ADMIN || isOwner(role)) {
                return true;
            }

            // For STAFF, check if they have access to the requested screen
            if (isStaff(role)) {
                String screenId = permission != null ? permission.toString() : null;
                if (screenId == null) {
                    return false;
                }

                List<String> allowedScreens = user.getAllowedScreens();
                if (allowedScreens == null || allowedScreens.isEmpty()) {
                    return false;
                }

                return allowedScreens.contains(screenId);
            }

            return false;
        } catch (Exception e) {
            log.error("Error evaluating permission: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return hasPermission(authentication, targetType, permission);
    }

    private String extractTokenFromAuthentication(Authentication authentication) {
        if (authentication.getCredentials() instanceof String) {
            return (String) authentication.getCredentials();
        }
        // If token is stored differently, adjust this method
        return null;
    }

    private boolean isOwner(Role role) {
        return role == Role.SUPPLIER_OWNER || role == Role.CUSTOMER_OWNER;
    }

    private boolean isStaff(Role role) {
        return role == Role.SUPPLIER_STAFF || role == Role.CUSTOMER_STAFF;
    }
}
