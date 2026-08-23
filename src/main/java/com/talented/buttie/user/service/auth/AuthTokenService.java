package com.talented.buttie.user.service.auth;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.common.security.AccountType;
import com.talented.buttie.common.security.TokenAccount;
import com.talented.buttie.common.security.jwt.JwtTokenProvider;
import com.talented.buttie.common.security.redis.RefreshTokenRepository;
import com.talented.buttie.user.dto.response.auth.AuthTokenResponse;
import com.talented.buttie.user.exception.AuthErrorCode;
import com.talented.buttie.user.mapper.AuthMapper;
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
    private final AuthMapper authMapper;

    public AuthTokenResponse createToken(Long userId) {
        validateActiveUserForTokenIssue(userId);

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
            refreshTokenRepository.invalidateAccessTokens(
                targetUserId,
                AccountType.USER,
                jwtTokenProvider.getAccessTokenExpiration()
            );
        } catch (DataAccessException e) {
            throw ApplicationException.from(AuthErrorCode.REFRESH_TOKEN_DELETE_FAILED);
        }

        return targetUserId;
    }

    public AuthTokenResponse reissue(String refreshToken) {
        TokenAccount account = parseUserRefreshToken(refreshToken);

        if (refreshTokenRepository.matches(account.accountId(), account.accountType(), refreshToken)) {
            throw ApplicationException.from(AuthErrorCode.INVALID_TOKEN);
        }
        return createToken(account.accountId());
    }

    public String reissueAccessToken(String refreshToken) {
        TokenAccount account = parseUserRefreshToken(refreshToken);

        if (refreshTokenRepository.matches(account.accountId(), account.accountType(), refreshToken)) {
            throw ApplicationException.from(AuthErrorCode.INVALID_TOKEN);
        }

        validateActiveUserForTokenIssue(account.accountId());
        return jwtTokenProvider.createUserAccessToken(account.accountId());
    }

    private TokenAccount parseUserRefreshToken(String refreshToken) {
        try {
            TokenAccount account = jwtTokenProvider.parseRefreshToken(refreshToken);
            if (account.accountType() != AccountType.USER) {
                throw ApplicationException.from(AuthErrorCode.INVALID_TOKEN);
            }
            return account;
        } catch (IllegalArgumentException e) {
            throw ApplicationException.from(AuthErrorCode.INVALID_TOKEN);
        }
    }

    private void validateActiveUserForTokenIssue(Long userId) {
        if (authMapper.findActiveUserIdForUpdate(userId) == null) {
            expirationToken(userId);
            throw ApplicationException.from(AuthErrorCode.INVALID_TOKEN);
        }
    }
}
