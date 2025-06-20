package com.travelapp.backend.global.filter;

import com.travelapp.backend.global.util.CookieUtil;
import com.travelapp.backend.global.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String token = resolveToken(request);

            if (token != null && jwtUtil.validateToken(token)) {
                Authentication authentication = getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Security Context에 사용자 ID '{}' 인증 정보를 저장했습니다.",
                    authentication.getPrincipal());
            } else {
                log.debug("유효한 JWT 토큰이 없습니다. URI: {}", request.getRequestURI());
            }
        } catch (Exception e) {
            log.error("JWT 인증 처리 중 오류 발생: {}, URI: {}", e.getMessage(), request.getRequestURI());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Request Header 또는 Cookie에서 토큰 정보를 추출
     * 우선순위: Authorization Header > Cookie
     */
    private String resolveToken(HttpServletRequest request) {
        // 1. Authorization Header에서 먼저 확인 (API 클라이언트용)
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            log.debug("Authorization Header에서 토큰 추출");
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        // 2. Cookie에서 Access Token 확인 (웹 브라우저 용)
        String cookieToken = CookieUtil.getAccessTokenFromCookie(request);
        if (StringUtils.hasText(cookieToken)) {
            log.debug("쿠키에서 Access Token 추출");
            return cookieToken;
        }

        log.debug("토큰을 찾을 수 없음 - Header: {}, Cookie: {}", bearerToken != null, cookieToken != null);
        return null;
    }

    /**
     * JWT 토큰에서 인증 정보 추출
     */
    private Authentication getAuthentication(String token) {
        try {
            Long memberId = jwtUtil.getMemberIdFromToken(token);
            String email = jwtUtil.getEmailFromToken(token);

            log.debug("JWT 토큰에서 사용자 정보 추출 성공 - ID: {}, Email: {}", memberId, email);

            // 사용자 권한 설정 (현재는 기본 권한만 부여)
            return new UsernamePasswordAuthenticationToken(
                memberId, // principal
                null, // credentials
                AuthorityUtils.createAuthorityList("ROLE_USER") // authorities
            );
        } catch (Exception e) {
            log.error("JWT 토큰에서 인증 정보 추출 실패: {}", e.getMessage());
            throw e;
        }
    }


}
