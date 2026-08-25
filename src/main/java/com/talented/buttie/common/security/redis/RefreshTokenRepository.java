package com.talented.buttie.common.security.redis;

import com.talented.buttie.common.security.AccountType;
import java.time.Duration;
import java.time.Instant;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RefreshTokenRepository {

    private static final String KEY_PREFIX = "refresh-token:";
    private static final String ACCESS_TOKEN_INVALIDATED_AT_KEY_PREFIX = "access-token-invalidated-at:";

    private final StringRedisTemplate redisTemplate;

    public RefreshTokenRepository(
        StringRedisTemplate redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    public void save(
        Long accountId,
        AccountType accountType,
        String refreshToken,
        long expirationMillis
    ) {
        redisTemplate.opsForValue().set(
            createKey(accountId, accountType),
            refreshToken,
            Duration.ofMillis(expirationMillis)
        );
    }

    public String find(
        Long accountId,
        AccountType accountType
    ) {
        return redisTemplate.opsForValue().get(
            createKey(accountId, accountType)
        );
    }

    public boolean matches(
        Long accountId,
        AccountType accountType,
        String refreshToken
    ) {
        String storedRefreshToken = find(
            accountId,
            accountType
        );

        return storedRefreshToken == null
            || !storedRefreshToken.equals(refreshToken);
    }

    public void delete(
        Long accountId,
        AccountType accountType
    ) {
        redisTemplate.delete(
            createKey(accountId, accountType)
        );
    }

    public void invalidateAccessTokens(
        Long accountId,
        AccountType accountType,
        long accessTokenExpirationMillis
    ) {
        redisTemplate.opsForValue().set(
            createAccessTokenInvalidatedAtKey(accountId, accountType),
            String.valueOf(Instant.now().toEpochMilli()),
            Duration.ofMillis(accessTokenExpirationMillis)
        );
    }

    public boolean isAccessTokenInvalidated(
        Long accountId,
        AccountType accountType,
        long issuedAtMillis
    ) {
        String invalidatedAt = redisTemplate.opsForValue().get(
            createAccessTokenInvalidatedAtKey(accountId, accountType)
        );
        return invalidatedAt != null
            && issuedAtMillis <= Long.parseLong(invalidatedAt);
    }

    private String createKey(
        Long accountId,
        AccountType accountType
    ) {
        return KEY_PREFIX
            + accountType.name()
            + ":"
            + accountId;
    }

    private String createAccessTokenInvalidatedAtKey(
        Long accountId,
        AccountType accountType
    ) {
        return ACCESS_TOKEN_INVALIDATED_AT_KEY_PREFIX
            + accountType.name()
            + ":"
            + accountId;
    }
}
