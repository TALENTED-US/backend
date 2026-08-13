package com.talented.buttie.mydata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.mydata.domain.AccountVO;
import com.talented.buttie.mydata.domain.CardVO;
import com.talented.buttie.mydata.dto.response.MydataTransactionSyncResponse;
import com.talented.buttie.mydata.exception.MydataErrorCode;
import com.talented.buttie.mydata.mapper.AccountMapper;
import com.talented.buttie.mydata.mapper.CardMapper;
import com.talented.buttie.mydata.mapper.MydataConnectionMapper;
import com.talented.buttie.mydata.service.mydata.FixedExpenseCandidateService;
import com.talented.buttie.mydata.service.mydata.MydataAssetRegistrationService;
import com.talented.buttie.mydata.service.mydata.MydataConnectionValidator;
import com.talented.buttie.mydata.service.mydata.MydataDuplicateTransactionService;
import com.talented.buttie.mydata.service.mydata.MydataTransactionImportService;
import com.talented.buttie.mydata.service.mydata.MydataTransactionSyncService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MydataTransactionSyncServiceTest {

    @Mock
    private MydataConnectionValidator mydataConnectionValidator;
    @Mock
    private AccountMapper accountMapper;
    @Mock
    private CardMapper cardMapper;
    @Mock
    private MydataTransactionImportService mydataTransactionImportService;
    @Mock
    private MydataDuplicateTransactionService mydataDuplicateTransactionService;
    @Mock
    private FixedExpenseCandidateService fixedExpenseCandidateService;
    @Mock
    private MydataConnectionMapper mydataConnectionMapper;
    @InjectMocks
    private MydataTransactionSyncService service;

    @Test
    void 활성_자산의_거래를_동기화하고_분석한다() {
        AccountVO account = AccountVO.builder().accountId(1L).build();
        CardVO card = CardVO.builder().cardId(2L).build();
        given(accountMapper.findActiveByUserId(101L)).willReturn(List.of(account));
        given(cardMapper.findActiveByUserId(101L)).willReturn(List.of(card));
        given(mydataTransactionImportService.sync(
            org.mockito.ArgumentMatchers.eq(101L),
            any(MydataAssetRegistrationService.RegisteredAssets.class)
        )).willReturn(new MydataTransactionImportService.SyncResult(3, 1));
        given(mydataConnectionMapper.updateLastSyncedAt(
            org.mockito.ArgumentMatchers.eq(101L),
            org.mockito.ArgumentMatchers.eq("MOCK"),
            any(LocalDateTime.class)
        )).willReturn(1);
        given(mydataDuplicateTransactionService.excludeLikelyAccountDuplicates(101L))
            .willReturn(1);
        given(fixedExpenseCandidateService.findCandidates(101L)).willReturn(List.of());

        MydataTransactionSyncResponse result = service.syncAndAnalyze(101L);

        assertEquals(3, result.insertedTransactionCount());
        assertEquals(1, result.skippedTransactionCount());
        assertEquals(1, result.excludedDuplicateCount());
        verify(mydataConnectionValidator).validateConnected(101L);
        verify(mydataConnectionMapper).updateLastSyncedAt(
            org.mockito.ArgumentMatchers.eq(101L),
            org.mockito.ArgumentMatchers.eq("MOCK"),
            any(LocalDateTime.class)
        );
    }

    @Test
    void 등록된_활성_자산이_없으면_동기화하지_않는다() {
        given(accountMapper.findActiveByUserId(101L)).willReturn(List.of());
        given(cardMapper.findActiveByUserId(101L)).willReturn(List.of());

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> service.syncAndAnalyze(101L)
        );

        assertEquals(MydataErrorCode.MYDATA_ASSET_NOT_REGISTERED, exception.getCode());
    }
}
