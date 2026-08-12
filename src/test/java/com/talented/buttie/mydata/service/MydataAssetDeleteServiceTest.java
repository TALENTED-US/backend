package com.talented.buttie.mydata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.talented.buttie.mydata.domain.MydataAssetType;
import com.talented.buttie.mydata.dto.response.MydataAssetDeletionResponse;
import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.mydata.exception.MydataErrorCode;
import com.talented.buttie.mydata.mapper.AccountMapper;
import com.talented.buttie.mydata.mapper.CardMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MydataAssetDeleteServiceTest {

    @Mock
    private MydataConnectionValidator mydataConnectionValidator;
    @Mock
    private AccountMapper accountMapper;
    @Mock
    private CardMapper cardMapper;
    @InjectMocks
    private MydataAssetDeleteService service;

    @Test
    void 선택한_계좌를_비활성화한다() {
        given(accountMapper.deactivateByUserIdAndExternalId(101L, "ACCOUNT-1"))
            .willReturn(1);

        MydataAssetDeletionResponse result = service.deactivate(
            101L,
            MydataAssetType.ACCOUNT,
            "ACCOUNT-1"
        );

        assertEquals(MydataAssetType.ACCOUNT, result.assetType());
        assertEquals("ACCOUNT-1", result.assetId());
        verify(mydataConnectionValidator).validateConnected(101L);
        verify(accountMapper).deactivateByUserIdAndExternalId(101L, "ACCOUNT-1");
    }

    @Test
    void 선택한_카드를_비활성화한다() {
        given(cardMapper.deactivateByUserIdAndExternalId(101L, "CARD-1"))
            .willReturn(1);

        service.deactivate(101L, MydataAssetType.CARD, "CARD-1");

        verify(cardMapper).deactivateByUserIdAndExternalId(101L, "CARD-1");
    }

    @Test
    void 활성_자산을_찾지_못하면_연동_해제에_실패한다() {
        given(cardMapper.deactivateByUserIdAndExternalId(101L, "MISSING-CARD"))
            .willReturn(0);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> service.deactivate(101L, MydataAssetType.CARD, "MISSING-CARD")
        );

        assertEquals(MydataErrorCode.MYDATA_ASSET_NOT_FOUND, exception.getCode());
    }
}
