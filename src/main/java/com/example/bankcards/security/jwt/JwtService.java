package com.example.bankcards.security.jwt;


import com.example.bankcards.dto.JwtAuthenticationDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtService {
    private static final Logger LOGGER = LogManager.getLogger(JwtService.class);

    @Value("e3b5803c681b39f714fd550190d78d8087b38232e3d881aa794935b4fd4bd75d3fbb3b5b6234cafce4f8919ccf93d3cf406120d76bd5fdb6c52fb6b48ae0aca5")
    private String jwtSecret;

    public JwtAuthenticationDto generateAuthToken(String email, String roles) {
        JwtAuthenticationDto jwtDTO = new JwtAuthenticationDto();
        jwtDTO.setToken(generateJwtToken(email, roles));
        jwtDTO.setRefreshToken(generateRefreshToken(email));
        return jwtDTO;
    }

    public JwtAuthenticationDto refreshBaseToken(String email, String refreshToken, String roles) {
        JwtAuthenticationDto jwtDto = new JwtAuthenticationDto();
        jwtDto.setToken(generateJwtToken(email, roles));
        jwtDto.setRefreshToken(refreshToken);
        return jwtDto;
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return true;
        } catch (ExpiredJwtException e) {
            LOGGER.error("Expired JwtException", e);
        } catch (UnsupportedJwtException e) {
            LOGGER.error("Unsupported JwtException", e);
        } catch (MalformedJwtException e) {
            LOGGER.error("Malformed JwtException", e);
        } catch (SecurityException e) {
            LOGGER.error("Security Exception", e);
        } catch (Exception e) {
            LOGGER.error("Exception", e);
        }
        return false;
    }

    private String generateJwtToken(String email, String roles) {
        Date date = Date.from(LocalDateTime.now().plusMinutes(5).atZone(ZoneId.systemDefault()).toInstant());
        return Jwts
                .builder()
                .subject(email)
                .claim("roles", roles)
                .expiration(date)
                .signWith(getSignInKey())
                .compact();
    }

    private String generateRefreshToken(String email) {
        Date date = Date.from(LocalDateTime.now().plusDays(1).atZone(ZoneId.systemDefault()).toInstant());
        return Jwts
                .builder()
                .subject(email)
                .expiration(date)
                .signWith(getSignInKey())
                .compact();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
