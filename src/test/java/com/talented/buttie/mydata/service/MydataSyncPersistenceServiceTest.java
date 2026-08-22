package com.talented.buttie.mydata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.mydata.dto.response.FixedExpenseCandidateResponse;
import com.talented.buttie.mydata.dto.response.MydataTransactionSyncResponse;
import com.talented.buttie.mydata.exception.MydataErrorCode;
import com.talented.buttie.mydata.mapper.MydataConnectionMapper;
import com.talented.buttie.mydata.service.mydata.FixedExpenseCandidateService;
import com.talented.buttie.mydata.service.mydata.MydataDuplicateTransactionService;
import com.talented.buttie.mydata.service.mydata.MydataSyncPersistenceService;
import com.talented.buttie.mydata.service.mydata.MydataTransactionImportService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MydataSyncPersistenceServiceTest {

    @Mock
    private MydataTransactionImportService mydataTransactionImportService;
    @Mock
    private MydataDuplicateTransactionService mydataDuplicateTransactionService;
    @Mock
    private FixedExpenseCandidateService fixedExpenseCandidateService;
    @Mock
    private MydataConnectionMapper mydataConnectionMapper;
    @InjectMocks
    private MydataSyncPersistenceService service;

    @Test
    void 거래를_저장하고_마지막_동기화_시각과_고정지출_후보_개수를_함께_반환한다() {
        List<TransactionVO> candidates = List.of(
            TransactionVO.builder().userId(101L).accountId(1L).externalTransactionId("A-1").build()
        );
        given(mydataTransactionImportService.importTransactions(candidates))
            .willReturn(new MydataTransactionImportService.SyncResult(3, 1));
        given(mydataConnectionMapper.updateLastSyncedAt(
            eq(101L), eq("MOCK"), any(LocalDateTime.class)
        )).willReturn(1);
        given(mydataDuplicateTransactionService.excludeLikelyAccountDuplicates(101L))
            .willReturn(2);
        FixedExpenseCandidateResponse candidate = new FixedExpenseCandidateResponse(
            "T-1", "넷플릭스", null, null, 17_000, 4, 3
        );
        given(fixedExpenseCandidateService.findCandidates(101L))
            .willReturn(List.of(candidate, candidate));

        MydataTransactionSyncResponse result = service.persist(101L, candidates);

        assertEquals(3, result.insertedTransactionCount());
        assertEquals(1, result.skippedTransactionCount());
        assertEquals(2, result.excludedDuplicateCount());
        assertEquals(2, result.fixedExpenseCandidateCount());
        verify(mydataConnectionMapper).updateLastSyncedAt(
            eq(101L), eq("MOCK"), any(LocalDateTime.class)
        );
    }

    @Test
    void 마지막_동기화_시각_갱신에_실패하면_예외를_던진다() {
        List<TransactionVO> candidates = List.of();
        given(mydataTransactionImportService.importTransactions(candidates))
            .willReturn(new MydataTransactionImportService.SyncResult(0, 0));
        given(mydataConnectionMapper.updateLastSyncedAt(
            eq(101L), eq("MOCK"), any(LocalDateTime.class)
        )).willReturn(0);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> service.persist(101L, candidates)
        );

        assertEquals(MydataErrorCode.MYDATA_TRANSACTION_SYNC_FAILED, exception.getCode());
    }
}
