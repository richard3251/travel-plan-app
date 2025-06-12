package com.travelapp.backend.global.util;

import com.travelapp.backend.domain.auth.exception.InvalidTokenException;
import com.travelapp.backend.global.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    /**
     * JWT 토큰 생성
     */
    public String generateToken(Long memberId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
            .subject(String.valueOf(memberId))
            .claim("email", email)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey())
            .compact();
    }

    /**
     * 리프레시 토큰 생성 (7일간 우효)
     */
    public String generateRefreshToken(Long memberId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (7 * 24 * 60 * 60 * 1000L)); // 7일

        return Jwts.builder()
            .subject(String.valueOf(memberId))
            .claim("type", "refresh")
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey())
            .compact();
    }

    /**
     * 리프레시 토큰 여부 확인
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = getClaims(token);
            return "refresh".equals(claims.get("type", String.class));
        } catch (Exception e ) {
            return false;
        }
    }

    /**
     * JWT 토큰에서 memberId 추출
     */
    public Long getMemberIdFromToken(String token) {

        try {
            Claims claims = getClaims(token);
            return Long.parseLong(claims.getSubject());
        } catch (NumberFormatException e) {
            log.error("Invalid memberId format in JWT token: {}", e.getMessage());
            throw new InvalidTokenException("잘못된 사용자 ID 형식입니다.");
        } catch (Exception e) {
            log.error("Failed to extract memberId from token: {}", e.getMessage());
            throw new InvalidTokenException("토큰에서 사용자 ID를 추출할 수 없습니다");
        }
    }

    /**
     * JWT 토큰에서 사용자 email 추출
     */
    public String getEmailFromToken(String token) {

        try {
            Claims claims = getClaims(token);
            String email = claims.get("email", String.class);

            if (email == null) {
                throw new InvalidTokenException("토큰에 이메일 정보가 없습니다.");
            }
            return email;
        } catch (Exception e) {
            log.error("Failed to extract email from token: {}", e.getMessage());
            throw new InvalidTokenException("토큰에서 이메일을 추출할 수 없습니다.");
        }
    }

    /**
     * JWT 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims String is empty: {}", e.getMessage());
        }

        return false;
    }

    /**
     * JWT 토큰에서 Claims 추출
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    /**
     * JWT 토큰 만료 시간 확인
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.getExpiration();
    }

    /**
     * JWT 토큰 만료 여부 확인
     */
    public boolean isTokenExpired(String token) {

        try {
            Date expirationDate = getExpirationDateFromToken(token);
            return expirationDate.before(new Date());
        } catch (Exception e) {
            log.error("Failed to check token expiration: {}", e.getMessage());
            return true;
        }
    }


}
