package com.talented.buttie.mydata.service;

import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.mydata.client.MydataApiClient;
import com.talented.buttie.mydata.client.dto.MydataTokenResponse;
import com.talented.buttie.mydata.domain.MydataConnectionVO;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MydataTokenService {

    private final MydataApiClient mydataApiClient;
    private final PKCrypto pkCrypto;

    public MydataConnectionVO issueConnection(
        Long userId,
        String provider,
        String authorizationCode
    ) {
        MydataTokenResponse token = mydataApiClient.issueToken(authorizationCode);
        String encryptedRefreshToken = pkCrypto.encryptValue(token.getRefreshToken());
        LocalDateTime expiresAt = LocalDateTime.now()
            .plusSeconds(token.getRefreshTokenExpiresIn());

        return MydataConnectionVO.connected(
            userId,
            provider,
            encryptedRefreshToken,
            expiresAt
        );
    }
}
