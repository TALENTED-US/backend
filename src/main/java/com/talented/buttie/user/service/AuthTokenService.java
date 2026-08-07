package com.talented.buttie.user.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.common.security.AccountType;
import com.talented.buttie.common.security.TokenAccount;
import com.talented.buttie.common.security.jwt.JwtTokenProvider;
import com.talented.buttie.common.security.redis.RefreshTokenRepository;
import com.talented.buttie.user.dto.response.auth.AuthTokenResponse;
import com.talented.buttie.user.exception.AuthErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthTokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthTokenResponse createToken(Long userId) {
        String accessToken = jwtTokenProvider.createUserAccessToken(userId);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId, AccountType.USER);
        long refreshTokenExpiration = jwtTokenProvider.getRefreshTokenExpiration();

        try {
            refreshTokenRepository.save(
                userId,
                AccountType.USER,
                refreshToken,
                refreshTokenExpiration
            );
        } catch (DataAccessException e) {
            throw ApplicationException.from(AuthErrorCode.REFRESH_TOKEN_SAVE_FAILED);
        }

        return new AuthTokenResponse(accessToken, refreshToken, refreshTokenExpiration);
    }

    public Long expirationToken(Long targetUserId) {
        try {
            refreshTokenRepository.delete(targetUserId, AccountType.USER);
        } catch (DataAccessException e) {
            throw ApplicationException.from(AuthErrorCode.REFRESH_TOKEN_DELETE_FAILED);
        }

        return targetUserId;
    }

    public AuthTokenResponse reissue(String refreshToken) {
        TokenAccount account;

        try {
            account = jwtTokenProvider.parseRefreshToken(refreshToken);
        } catch (IllegalArgumentException e) {
            throw ApplicationException.from(AuthErrorCode.INVALID_TOKEN);
        }

        if(account.accountType() != AccountType.USER) {
            throw ApplicationException.from(AuthErrorCode.INVALID_TOKEN);
        }

        if(!refreshTokenRepository.matches(account.accountId(), account.accountType(), refreshToken)){
            throw ApplicationException.from(AuthErrorCode.INVALID_TOKEN);
        }
        return createToken(account.accountId());
    }
}
