package com.talented.buttie.mydata.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.mydata.domain.ConnectionStatus;
import com.talented.buttie.mydata.domain.MydataConnectionVO;
import com.talented.buttie.mydata.exception.MydataErrorCode;
import com.talented.buttie.mydata.mapper.MydataConnectionMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MydataConnectionValidatorTest {

    @Mock
    private MydataConnectionMapper mydataConnectionMapper;
    @InjectMocks
    private MydataConnectionValidator validator;

    @Test
    void 연결상태이고_리프레시토큰이_유효하면_통과한다() {
        given(mydataConnectionMapper.findByUserIdAndProvider(101L, "MOCK"))
            .willReturn(connection(ConnectionStatus.CONNECTED, LocalDateTime.now().plusDays(1)));

        assertDoesNotThrow(() -> validator.validateConnected(101L));
    }

    @Test
    void 리프레시토큰이_만료되면_자산조회가_거부된다() {
        given(mydataConnectionMapper.findByUserIdAndProvider(101L, "MOCK"))
            .willReturn(connection(ConnectionStatus.CONNECTED, LocalDateTime.now().minusSeconds(1)));

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> validator.validateConnected(101L)
        );

        assertEquals(MydataErrorCode.MYDATA_NOT_CONNECTED, exception.getCode());
    }

    @Test
    void 연결상태가_아니면_자산조회가_거부된다() {
        given(mydataConnectionMapper.findByUserIdAndProvider(101L, "MOCK"))
            .willReturn(connection(ConnectionStatus.REVOKED, LocalDateTime.now().plusDays(1)));

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> validator.validateConnected(101L)
        );

        assertEquals(MydataErrorCode.MYDATA_NOT_CONNECTED, exception.getCode());
    }

    private MydataConnectionVO connection(
        ConnectionStatus status,
        LocalDateTime refreshTokenExpiresAt
    ) {
        return MydataConnectionVO.builder()
            .mydataStatus(status)
            .refreshTokenEncrypted("encrypted-refresh-token")
            .refreshTokenExpiresAt(refreshTokenExpiresAt)
            .build();
    }
}
