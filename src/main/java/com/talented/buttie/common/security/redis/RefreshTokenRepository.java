package com.talented.buttie.common.security.redis;

import com.talented.buttie.common.security.AccountType;
import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RefreshTokenRepository {

    private static final String KEY_PREFIX = "refresh-token:";

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

        return storedRefreshToken != null
            && storedRefreshToken.equals(refreshToken);
    }

    public void delete(
        Long accountId,
        AccountType accountType
    ) {
        redisTemplate.delete(
            createKey(accountId, accountType)
        );
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
}