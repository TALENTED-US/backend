package com.talented.buttie.common.security.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.common.security.AccountType;
import com.talented.buttie.common.security.TokenAccount;
import com.talented.buttie.common.security.redis.RefreshTokenRepository;
import com.talented.buttie.user.exception.AuthErrorCode;
import com.talented.buttie.user.mapper.AuthMapper;
import com.talented.buttie.user.service.auth.AuthTokenService;
import org.junit.jupiter.api.Test;

class AuthTokenServiceTest {

    @Test
    void 탈퇴한_사용자의_Refresh_Token을_폐기하고_재발급을_거부한다() {
        JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
        RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
        AuthMapper authMapper = mock(AuthMapper.class);
        AuthTokenService authTokenService = new AuthTokenService(
            jwtTokenProvider,
            refreshTokenRepository,
            authMapper
        );
        when(jwtTokenProvider.parseRefreshToken("refresh-token"))
            .thenReturn(new TokenAccount(1L, AccountType.USER));
        when(authMapper.findActiveUserIdForUpdate(1L)).thenReturn(null);
        when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(1000L);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> authTokenService.reissue("refresh-token")
        );

        assertEquals(AuthErrorCode.INVALID_TOKEN, exception.getCode());
        verify(refreshTokenRepository).delete(1L, AccountType.USER);
        verify(refreshTokenRepository).invalidateAccessTokens(1L, AccountType.USER, 1000L);
    }
}
