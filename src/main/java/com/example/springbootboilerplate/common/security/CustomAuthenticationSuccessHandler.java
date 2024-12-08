package com.example.springbootboilerplate.common.security;

import com.example.springbootboilerplate.common.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON 변환용

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                        .findFirst().orElse(null);

        String userId = oAuth2User.getAttribute("userId");

        String accessToken = jwtUtil.generateAccessToken(userId, role);
        String refreshToken = jwtUtil.generateRefreshToken(userId);

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put(jwtUtil.getAccessTokenName(), accessToken);
        // 쿠키 생성
        Cookie refreshTokenCookie = getRefreshTokenCookie(refreshToken);
        // 쿠키를 클라이언트의 응답에 추가
        response.addCookie(refreshTokenCookie);

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        // JSON 응답 전송
        response.getWriter().write(objectMapper.writeValueAsString(responseBody));
    }

    private Cookie getRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie(jwtUtil.getRefreshTokenName(), refreshToken);
        cookie.setPath("/");
        // 쿠키의 만료 시간 설정 (refreshTokenExpirationTime과 같게 설정)
        cookie.setMaxAge( jwtUtil.getRefreshTokenExpirationTime().intValue() / 1000);
        // 쿠키가 HTTP 요청에서만 사용되도록 설정 (JavaScript에서 접근 불가)
        cookie.setHttpOnly(true);
        // 쿠키가 동일 사이트에서만 유효하도록 설정
        // "SameSite" 속성 수동으로 추가
        cookie.setAttribute("SameSite", "Strict");  // 또는 "Lax"
        // 쿠키가 HTTPS에서만 전송되도록 설정 (보안)
//        cookie.setSecure(true);  // HTTPS 연결일 때만 사용
        return cookie;
    }
}
