package com.talented.buttie.common.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CsrfProtectionFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String logoutPath = request.getContextPath() + "/api/auth/logout";

        return !"DELETE".equalsIgnoreCase(request.getMethod())
            || !logoutPath.equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String csrfToken = findCookieValue(
            request,
            SecurityConstants.CSRF_TOKEN_COOKIE_NAME
        );
        String csrfHeader = request.getHeader(
            SecurityConstants.CSRF_TOKEN_HEADER_NAME
        );

        if (!isValid(csrfToken, csrfHeader)) {
            response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "CSRF 토큰이 유효하지 않습니다."
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String findCookieValue(
        HttpServletRequest request,
        String cookieName
    ) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private boolean isValid(String csrfToken, String csrfHeader) {
        return csrfToken != null
            && csrfHeader != null
            && MessageDigest.isEqual(
                csrfToken.getBytes(StandardCharsets.UTF_8),
                csrfHeader.getBytes(StandardCharsets.UTF_8)
            );
    }
}
