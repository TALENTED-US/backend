package com.talented.buttie.common.security.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.common.security.AccountType;
import com.talented.buttie.common.security.SecurityConstants;
import com.talented.buttie.common.security.TokenAccount;
import com.talented.buttie.common.security.redis.RefreshTokenRepository;
import com.talented.buttie.user.exception.AuthErrorCode;
import com.talented.buttie.user.mapper.AuthMapper;
import com.talented.buttie.user.service.auth.AuthTokenService;
import io.jsonwebtoken.Claims;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class JwtAuthenticationFilterTest {

    @Test
    void 탈퇴한_사용자의_Access_Token을_거부한다() throws Exception {
        JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
        RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
        AuthMapper authMapper = mock(AuthMapper.class);
        AuthTokenService authTokenService = mock(AuthTokenService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
            jwtTokenProvider,
            refreshTokenRepository,
            authMapper,
            authTokenService
        );
        Claims claims = mock(Claims.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        request.addHeader(SecurityConstants.AUTHORIZATION_HEADER, "Bearer access-token");
        when(jwtTokenProvider.parseAccessToken("access-token")).thenReturn(claims);
        when(claims.get(JwtClaim.ACCOUNT_TYPE, String.class)).thenReturn(AccountType.USER.name());
        when(claims.getSubject()).thenReturn("1");
        when(claims.get(JwtClaim.ISSUED_AT_MILLIS, Long.class)).thenReturn(1L);
        when(refreshTokenRepository.isAccessTokenInvalidated(1L, AccountType.USER, 1L))
            .thenReturn(false);
        when(authMapper.existsActiveUserById(1L)).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void 탈퇴한_사용자는_만료된_Access_Token으로_재발급할_수_없다() throws Exception {
        JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
        RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
        AuthMapper authMapper = mock(AuthMapper.class);
        AuthTokenService authTokenService = mock(AuthTokenService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
            jwtTokenProvider,
            refreshTokenRepository,
            authMapper,
            authTokenService
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        request.addHeader(SecurityConstants.AUTHORIZATION_HEADER, "Bearer expired-access-token");
        request.setCookies(new javax.servlet.http.Cookie(
            SecurityConstants.REFRESH_TOKEN_COOKIE_NAME,
            "refresh-token"
        ));
        when(jwtTokenProvider.parseAccessToken("expired-access-token"))
            .thenThrow(new JwtTokenExpiredException("expired", null));
        when(jwtTokenProvider.parseRefreshToken("refresh-token"))
            .thenReturn(new TokenAccount(1L, AccountType.USER));
        when(authTokenService.reissueAccessToken("refresh-token"))
            .thenThrow(ApplicationException.from(AuthErrorCode.INVALID_TOKEN));

        filter.doFilter(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
    }
}
