package com.talented.buttie.mydata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.mydata.client.MydataApiClient;
import com.talented.buttie.mydata.client.dto.MydataAuthorizationResponse;
import com.talented.buttie.mydata.domain.MydataConnectionVO;
import com.talented.buttie.mydata.exception.MydataErrorCode;
import com.talented.buttie.mydata.mapper.MydataConnectionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MydataConnectionServiceTest {

    @Mock
    private MydataApiClient mydataApiClient;
    @Mock
    private MydataTokenService mydataTokenService;
    @Mock
    private MydataConnectionMapper mydataConnectionMapper;
    @InjectMocks
    private MydataConnectionService mydataConnectionService;

    @Test
    void userId를_CI로_인가받고_연결정보를_저장한다() {
        MydataAuthorizationResponse authorization = new MydataAuthorizationResponse(
            "mock-auth-code-user-1",
            "state",
            "transaction-id"
        );
        MydataConnectionVO connection = MydataConnectionVO.builder()
            .mydataId(10L)
            .userId(101L)
            .provider("MOCK")
            .build();

        given(mydataConnectionMapper.findByUserIdAndProvider(101L, "MOCK"))
            .willReturn(null);
        given(mydataApiClient.authorize(101L)).willReturn(authorization);
        given(mydataTokenService.issueConnection(101L, "MOCK", authorization.getCode()))
            .willReturn(connection);
        given(mydataConnectionMapper.insert(connection)).willReturn(1);

        MydataConnectionVO result = mydataConnectionService.connect(101L);

        assertSame(connection, result);
        verify(mydataApiClient).authorize(101L);
        verify(mydataConnectionMapper).insert(connection);
    }

    @Test
    void 이미_연결된_사용자는_다시_인증하지_않는다() {
        given(mydataConnectionMapper.findByUserIdAndProvider(101L, "MOCK"))
            .willReturn(MydataConnectionVO.builder().userId(101L).provider("MOCK").build());

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> mydataConnectionService.connect(101L)
        );

        assertEquals(MydataErrorCode.MYDATA_ALREADY_CONNECTED, exception.getCode());
        verify(mydataApiClient, never()).authorize(101L);
    }
}
