package com.talented.buttie.mydata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.mydata.domain.AccountVO;
import com.talented.buttie.mydata.domain.CardVO;
import com.talented.buttie.mydata.dto.response.MydataTransactionSyncResponse;
import com.talented.buttie.mydata.exception.MydataErrorCode;
import com.talented.buttie.mydata.mapper.AccountMapper;
import com.talented.buttie.mydata.mapper.CardMapper;
import com.talented.buttie.mydata.service.mydata.MydataConnectionValidator;
import com.talented.buttie.mydata.service.mydata.MydataSyncPersistenceService;
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
    private MydataSyncPersistenceService mydataSyncPersistenceService;
    @InjectMocks
    private MydataTransactionSyncService service;

    @Test
    void 활성_자산의_외부_거래를_모아_영속화_단계에_넘긴다() {
        AccountVO account = AccountVO.builder().accountId(1L).build();
        CardVO card = CardVO.builder().cardId(2L).build();
        List<TransactionVO> candidates = List.of(
            TransactionVO.builder().userId(101L).accountId(1L).externalTransactionId("A-1").build()
        );
        MydataTransactionSyncResponse expected = MydataTransactionSyncResponse.builder()
            .insertedTransactionCount(3)
            .skippedTransactionCount(1)
            .excludedDuplicateCount(1)
            .fixedExpenseCandidateCount(2)
            .lastSyncedAt(LocalDateTime.now())
            .build();

        given(accountMapper.findActiveByUserId(101L)).willReturn(List.of(account));
        given(cardMapper.findActiveByUserId(101L)).willReturn(List.of(card));
        given(mydataTransactionImportService.fetchExternalTransactions(eq(101L), any()))
            .willReturn(candidates);
        given(mydataSyncPersistenceService.persist(eq(101L), eq(candidates)))
            .willReturn(expected);

        MydataTransactionSyncResponse result = service.syncAndAnalyze(101L);

        assertEquals(expected, result);
        verify(mydataConnectionValidator).validateConnected(101L);
        verify(mydataSyncPersistenceService).persist(101L, candidates);
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
