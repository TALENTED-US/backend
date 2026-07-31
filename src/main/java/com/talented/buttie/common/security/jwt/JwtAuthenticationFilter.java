package com.talented.buttie.common.security.jwt;

import com.talented.buttie.common.security.AccountType;
import com.talented.buttie.common.security.AdminRole;
import com.talented.buttie.common.security.AuthenticationUser;
import com.talented.buttie.common.security.SecurityConstants;
import com.talented.buttie.common.security.TokenAccount;
import com.talented.buttie.common.security.redis.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtAuthenticationFilter(
        JwtTokenProvider jwtTokenProvider,
        RefreshTokenRepository refreshTokenRepository
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtTokenProvider.parseAccessToken(token);
            AuthenticationUser authenticationUser = createAuthenticationUser(claims);
            continueWithAuthenticatedUser(
                request,
                response,
                filterChain,
                authenticationUser
            );
        } catch (JwtTokenExpiredException e) {
            try {
                AuthenticationUser authenticationUser = reissueUserAccessToken(
                    request,
                    response
                );
                continueWithAuthenticatedUser(
                    request,
                    response,
                    filterChain,
                    authenticationUser
                );
            } catch (IllegalArgumentException refreshTokenException) {
                response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Refresh Token이 유효하지 않습니다."
                );
            }
        } catch (IllegalArgumentException e) {
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "유효하지 않은 Access Token입니다."
            );
        }
    }

    private AuthenticationUser reissueUserAccessToken(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        String refreshToken = resolveRefreshToken(request);

        if (refreshToken == null) {
            throw new IllegalArgumentException("Refresh Token이 없습니다.");
        }

        TokenAccount tokenAccount = jwtTokenProvider.parseRefreshToken(
            refreshToken
        );

        if (
            tokenAccount.accountType() != AccountType.USER ||
            !refreshTokenRepository.matches(
                tokenAccount.accountId(),
                tokenAccount.accountType(),
                refreshToken
            )
        ) {
            throw new IllegalArgumentException("Refresh Token이 유효하지 않습니다.");
        }

        String newAccessToken = jwtTokenProvider.createUserAccessToken(
            tokenAccount.accountId()
        );
        response.setHeader(
            SecurityConstants.AUTHORIZATION_HEADER,
            SecurityConstants.BEARER_PREFIX + newAccessToken
        );

        return AuthenticationUser.user(tokenAccount.accountId());
    }

    private void continueWithAuthenticatedUser(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain,
        AuthenticationUser authenticationUser
    ) throws IOException, ServletException {
        request.setAttribute(
            SecurityConstants.AUTHENTICATION_USER_ATTRIBUTE,
            authenticationUser
        );
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(
            SecurityConstants.AUTHORIZATION_HEADER
        );

        if (
            authorizationHeader == null ||
                !authorizationHeader.startsWith(SecurityConstants.BEARER_PREFIX)
        ) {
            return null;
        }

        String token = authorizationHeader.substring(
            SecurityConstants.BEARER_PREFIX.length()
        ).trim();

        return token.isEmpty() ? null : token;
    }

    private String resolveRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (
                SecurityConstants.REFRESH_TOKEN_COOKIE_NAME.equals(
                    cookie.getName()
                )
            ) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private AuthenticationUser createAuthenticationUser(Claims claims) {
        Long accountId = Long.valueOf(claims.getSubject());
        AccountType accountType = AccountType.valueOf(
            claims.get(JwtClaim.ACCOUNT_TYPE, String.class)
        );

        if (accountType == AccountType.USER) {
            return AuthenticationUser.user(accountId);
        }

        if (accountType == AccountType.ADMIN) {
            AdminRole adminRole = AdminRole.valueOf(
                claims.get(JwtClaim.ADMIN_ROLE, String.class)
            );
            return AuthenticationUser.admin(accountId, adminRole);
        }

        throw new IllegalArgumentException("지원하지 않는 계정 유형입니다.");
    }
}
