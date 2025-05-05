package com.rawneeded.jwt;

import com.rawneeded.dto.auth.GenerateTokenDto;
import com.rawneeded.enummeration.Role;
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
        return Jwts.builder()
                .claim("id", dto.getId())
                .claim("name" , dto.getName())
                .claim("email",dto.getEmail())
                .claim("role", dto.getRole())
                .setSubject(dto.getName())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
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

            return generateToken(GenerateTokenDto.builder()
                    .name(username)
                    .email(email)
                    .role(Role.valueOf(role))
                    .id(userId)
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


    public String getEmailFromToken(String token) {
            return getClaimFromToken(token, "email");
        }


        public String getRoleFromToken(String token) {
            return getClaimFromToken(token, "role");
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


