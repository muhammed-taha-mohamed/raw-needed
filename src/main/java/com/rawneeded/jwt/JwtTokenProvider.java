package com.rawneeded.jwt;

import com.rawneeded.dto.auth.GenerateTokenDto;
import com.rawneeded.enumeration.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;


@Component
    public class JwtTokenProvider {

        @Value("${jwt.secret}")
        private String jwtSecret;

        @Value("${jwt.expiration}")
        private long jwtExpiration;


    public String generateToken(GenerateTokenDto dto) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        var builder = Jwts.builder()
                .claim("id", dto.getId())
                .claim("name" , dto.getName())
                .claim("email",dto.getEmail())
                .claim("role", dto.getRole())
                .claim("ownerId", dto.getOwnerId()!=null?dto.getOwnerId():dto.getId())
                .claim("phoneNumber", dto.getPhoneNumber())
                .setSubject(dto.getName())
                .setIssuedAt(now)
                .setExpiration(expiryDate);
        if (dto.getSessionId() != null && !dto.getSessionId().isEmpty()) {
            builder.claim("sessionId", dto.getSessionId());
        }
        return builder.signWith(SignatureAlgorithm.HS512, jwtSecret).compact();
    }





        public String refreshToken(String token) {
            Claims claims;
            try {
                claims = Jwts.parser()
                        .setSigningKey(jwtSecret)
                        .parseClaimsJws(token)
                        .getBody();
            } catch (ExpiredJwtException e) {
                claims = e.getClaims();
            }

            String username = claims.getSubject();
            String userId = claims.get("id", String.class);
            String email = claims.get("email", String.class);
            String role = claims.get("role", String.class);
            String ownerId = claims.get("ownerId", String.class);
            String phoneNumber = claims.get("phoneNumber", String.class);
            String sessionId = claims.get("sessionId", String.class);

            return generateToken(GenerateTokenDto.builder()
                    .name(username)
                    .email(email)
                    .role(Role.valueOf(role))
                    .id(userId)
                    .ownerId(ownerId)
                    .phoneNumber(phoneNumber)
                    .sessionId(sessionId)
                    .build());
        }




        private String getClaimFromToken(String token, String key) {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get(key, String.class);
        }


        public String getUsernameFromToken(String token) {
            return getClaimFromToken(token, Claims.SUBJECT);
        }

    public String getIdFromToken(String token) {
        if (token.startsWith("bearer ") || token.startsWith("Bearer ")) {
            return getClaimFromToken(token.substring(7), "id");
        }
        return getClaimFromToken(token, "id");
    }

    public String getPhoneNumberFromToken(String token) {
        if (token.startsWith("bearer ") || token.startsWith("Bearer ")) {
            return getClaimFromToken(token.substring(7), "phoneNumber");
        }
        return getClaimFromToken(token, "phoneNumber");
    }
    public String getOwnerIdFromToken(String token) {
        if (token.startsWith("bearer ") || token.startsWith("Bearer ")) {
            return getClaimFromToken(token.substring(7), "ownerId");
        }
        return getClaimFromToken(token, "ownerId");
    }

    public String getSessionIdFromToken(String token) {
        String t = token;
        if (token.startsWith("bearer ") || token.startsWith("Bearer ")) {
            t = token.substring(7);
        }
        try {
            return getClaimFromToken(t, "sessionId");
        } catch (Exception e) {
            return null;
        }
    }

    public String getEmailFromToken(String token) {
            return getClaimFromToken(token, "email");
        }


        public Role getRoleFromToken(String token) {
            return Role.valueOf(getClaimFromToken(token, "role"));
        }

        public boolean validateToken (String token) {
            try {
                Jwts.parser()
                        .setSigningKey(jwtSecret)
                        .parseClaimsJws(token);
                return true;
            } catch (ExpiredJwtException e) {
                return false;
            } catch (Exception e) {
                return false;
            }
        }

    }


