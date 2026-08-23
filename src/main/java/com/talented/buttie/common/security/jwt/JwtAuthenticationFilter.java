package com.talented.buttie.common.security.jwt;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.common.security.AccountType;
import com.talented.buttie.common.security.AdminRole;
import com.talented.buttie.common.security.AuthenticationUser;
import com.talented.buttie.common.security.SecurityConstants;
import com.talented.buttie.common.security.TokenAccount;
import com.talented.buttie.common.security.redis.RefreshTokenRepository;
import com.talented.buttie.user.mapper.AuthMapper;
import com.talented.buttie.user.service.auth.AuthTokenService;
import io.jsonwebtoken.Claims;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthMapper authMapper;
    private final AuthTokenService authTokenService;

    public JwtAuthenticationFilter(
        JwtTokenProvider jwtTokenProvider,
        RefreshTokenRepository refreshTokenRepository,
        AuthMapper authMapper,
        AuthTokenService authTokenService
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.authMapper = authMapper;
        this.authTokenService = authTokenService;
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtTokenProvider.parseAccessToken(token);
            validateAccessTokenNotInvalidated(claims);
            AuthenticationUser authenticationUser = createAuthenticationUser(claims);
            validateActiveUser(authenticationUser);
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

        if (tokenAccount.accountType() != AccountType.USER) {
            throw new IllegalArgumentException("Refresh Token이 유효하지 않습니다.");
        }

        String newAccessToken;
        try {
            newAccessToken = authTokenService.reissueAccessToken(refreshToken);
        } catch (ApplicationException e) {
            throw new IllegalArgumentException("Refresh Token이 유효하지 않습니다.", e);
        }
        response.setHeader(
            SecurityConstants.AUTHORIZATION_HEADER,
            SecurityConstants.BEARER_PREFIX + newAccessToken
        );

        return AuthenticationUser.user(tokenAccount.accountId());
    }

    private void validateActiveUser(AuthenticationUser authenticationUser) {
        if (authenticationUser.isUser()
            && !authMapper.existsActiveUserById(authenticationUser.userId())) {
            throw new IllegalArgumentException("탈퇴했거나 비활성화된 사용자입니다.");
        }
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

    private void validateAccessTokenNotInvalidated(Claims claims) {
        AccountType accountType = AccountType.valueOf(
            claims.get(JwtClaim.ACCOUNT_TYPE, String.class)
        );
        if (accountType != AccountType.USER) {
            return;
        }

        Long accountId = Long.valueOf(claims.getSubject());
        Long issuedAtMillis = claims.get(JwtClaim.ISSUED_AT_MILLIS, Long.class);
        if (issuedAtMillis == null) {
            issuedAtMillis = claims.getIssuedAt().getTime();
        }
        if (refreshTokenRepository.isAccessTokenInvalidated(accountId, accountType, issuedAtMillis)) {
            throw new IllegalArgumentException("폐기된 Access Token입니다.");
        }
    }
}
