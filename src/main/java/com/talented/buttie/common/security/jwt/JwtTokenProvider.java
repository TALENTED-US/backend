package com.talented.buttie.common.security.jwt;

import com.talented.buttie.common.security.AccountType;
import com.talented.buttie.common.security.AdminRole;
import com.talented.buttie.common.security.TokenAccount;
import com.talented.buttie.common.security.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final SecretKey signingKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.access-token-expiration}")
        long accessTokenExpiration,
        @Value("${jwt.refresh-token-expiration}")
        long refreshTokenExpiration
    ) {
        byte[] keyBytes = secret.getBytes();

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String createUserAccessToken(Long userId) {
        Instant now = Instant.now();

        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim(
                JwtClaim.ACCOUNT_TYPE,
                AccountType.USER.name()
            )
            .claim(
                JwtClaim.TOKEN_TYPE,
                TokenType.ACCESS.name()
            )
            .issuedAt(Date.from(now))
            .expiration(
                Date.from(
                    now.plusMillis(
                        accessTokenExpiration
                    )
                )
            )
            .signWith(signingKey)
            .compact();
    }

    public String createAdminAccessToken(
        Long adminId,
        AdminRole adminRole
    ) {
        Instant now = Instant.now();

        return Jwts.builder()
            .subject(String.valueOf(adminId))
            .claim(
                JwtClaim.ACCOUNT_TYPE,
                AccountType.ADMIN.name()
            )
            .claim(
                JwtClaim.ADMIN_ROLE,
                adminRole.name()
            )
            .claim(
                JwtClaim.TOKEN_TYPE,
                TokenType.ACCESS.name()
            )
            .issuedAt(Date.from(now))
            .expiration(
                Date.from(
                    now.plusMillis(
                        accessTokenExpiration
                    )
                )
            )
            .signWith(signingKey)
            .compact();
    }

    public String createRefreshToken(
        Long accountId,
        AccountType accountType
    ) {
        Instant now = Instant.now();

        return Jwts.builder()
            .subject(String.valueOf(accountId))
            .claim(
                JwtClaim.ACCOUNT_TYPE,
                accountType.name()
            )
            .claim(
                JwtClaim.TOKEN_TYPE,
                TokenType.REFRESH.name()
            )
            .issuedAt(Date.from(now))
            .expiration(
                Date.from(
                    now.plusMillis(
                        refreshTokenExpiration
                    )
                )
            )
            .signWith(signingKey)
            .compact();
    }

    public Claims parseAccessToken(String token) {
        Claims claims = parseClaims(token);

        String tokenType = claims.get(
            JwtClaim.TOKEN_TYPE,
            String.class
        );

        if (!TokenType.ACCESS.name().equals(tokenType)) {
            throw new IllegalArgumentException(
                "Access Token이 아닙니다."
            );
        }

        return claims;
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        } catch (ExpiredJwtException e) {
            throw new JwtTokenExpiredException(
                "만료된 토큰입니다.",
                e
            );

        } catch (JwtException | IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "유효하지 않은 토큰입니다.",
                e
            );
        }
    }

    public TokenAccount parseRefreshToken(String token) {
        Claims claims = parseClaims(token);

        String tokenType = claims.get(
            JwtClaim.TOKEN_TYPE,
            String.class
        );

        if (!TokenType.REFRESH.name().equals(tokenType)) {
            throw new IllegalArgumentException(
                "Refresh Token이 아닙니다."
            );
        }
        Long accountId;
        try {
            accountId = Long.valueOf(
                claims.getSubject()
            );
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException(
                "토큰의 계정 식별자가 올바르지 않습니다.",
                e
            );
        }

        AccountType accountType;
        try {
            accountType = AccountType.valueOf(
                claims.get(
                    JwtClaim.ACCOUNT_TYPE,
                    String.class
                )
            );
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException(
                "토큰의 계정 유형이 올바르지 않습니다.",
                e
            );
        }

        return new TokenAccount(
            accountId,
            accountType
        );
    }
}
