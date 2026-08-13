package com.talented.buttie.mydata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.mydata.client.MydataApiClient;
import com.talented.buttie.mydata.client.dto.MydataTokenResponse;
import com.talented.buttie.mydata.domain.ConnectionStatus;
import com.talented.buttie.mydata.domain.MydataConnectionVO;
import com.talented.buttie.mydata.service.mydata.MydataTokenService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MydataTokenServiceTest {

    @Mock
    private MydataApiClient mydataApiClient;
    @Mock
    private PKCrypto pkCrypto;
    @InjectMocks
    private MydataTokenService mydataTokenService;

    @Test
    void refreshToken을_암호화하고_만료시각을_계산한다() {
        MydataTokenResponse token = MydataTokenResponse.builder()
            .refreshToken("plain-refresh-token")
            .refreshTokenExpiresIn(2_592_000L)
            .build();
        given(mydataApiClient.issueToken("authorization-code")).willReturn(token);
        given(pkCrypto.encryptValue("plain-refresh-token")).willReturn("encrypted-token");
        LocalDateTime expectedFrom = LocalDateTime.now().plusSeconds(2_592_000L);

        MydataConnectionVO result = mydataTokenService.issueConnection(
            101L,
            "MOCK",
            "authorization-code"
        );

        LocalDateTime expectedTo = LocalDateTime.now().plusSeconds(2_592_000L);
        assertEquals(101L, result.getUserId());
        assertEquals("MOCK", result.getProvider());
        assertEquals("encrypted-token", result.getRefreshTokenEncrypted());
        assertEquals(ConnectionStatus.CONNECTED, result.getMydataStatus());
        assertFalse(result.getRefreshTokenExpiresAt().isBefore(expectedFrom));
        assertTrue(result.getRefreshTokenExpiresAt().isBefore(expectedTo.plusSeconds(1)));
    }
}
