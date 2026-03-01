/*
 * Copyright (c) 2024. Mohammed Taha
 */

package com.rawneeded.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.model.UserSession;
import com.rawneeded.repository.UserSessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@AllArgsConstructor
public class AuthorizationFilter extends OncePerRequestFilter {

    private static final Set<String> publicRequests = Set.of(
            "swagger",
            "v3/api-docs",
            "refresh-token",
            "auth",
            "swagger-ui",
            "api-docs",
            "sign-up",
            "category",
            "image",
            "dummy"
    );
    private final JwtTokenProvider jwtTokenUtil;
    private final UserSessionRepository userSessionRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) {
        try {
            String uri = request.getRequestURI();
            if (!isPublicRequest(uri)) {
                String token = extractToken(request.getHeader("Authorization"));
                if (token == null || !jwtTokenUtil.validateToken(token)) {
                    throwError(response);
                    return;
                }
                String userId = jwtTokenUtil.getIdFromToken(token);
                String sessionId = jwtTokenUtil.getSessionIdFromToken(token);
                Optional<UserSession> dbSession = userSessionRepository.findByUserId(userId);
                if (dbSession.isPresent()) {
                    if (sessionId == null || !sessionId.equals(dbSession.get().getCurrentSessionId())) {
                        throwError(response);
                        return;
                    }
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            return;
            // throwError(response);
        }
    }

    private boolean isPublicRequest(String uri) {
        return publicRequests.stream()
                .anyMatch(uri::contains);
    }

    private String extractToken(String header) {
        String BEARER = "Bearer ";
        if (header != null && header.startsWith(BEARER)) {
            return header.substring(7);
        }
        return null;
    }


    private void throwError(HttpServletResponse response) {
        String unauthorizedMessage = "Access Denied: Unauthorized Access Attempt!";
        try {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            ResponsePayload payload = ResponsePayload.builder()
                    .error(Map.of("errorMessage", unauthorizedMessage))
                    .build();
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(response.getWriter(), payload);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, unauthorizedMessage);
        }
    }
}
