package com.talented.buttie.mydata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

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
import com.talented.buttie.simulation.service.FinancialSnapshotCreateService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
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
    @Mock
    private FinancialSnapshotCreateService financialSnapshotCreateService;
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

        MydataTransactionSyncResponse result = service.persistAndRefreshSnapshot(101L, candidates);

        assertEquals(3, result.insertedTransactionCount());
        assertEquals(1, result.skippedTransactionCount());
        assertEquals(2, result.excludedDuplicateCount());
        assertEquals(2, result.fixedExpenseCandidateCount());
        verify(mydataConnectionMapper).updateLastSyncedAt(
            eq(101L), eq("MOCK"), any(LocalDateTime.class)
        );
        InOrder inOrder = inOrder(fixedExpenseCandidateService, financialSnapshotCreateService);
        inOrder.verify(fixedExpenseCandidateService).findCandidates(101L);
        inOrder.verify(financialSnapshotCreateService).createSnapshot(101L);
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
            () -> service.persistAndRefreshSnapshot(101L, candidates)
        );

        assertEquals(MydataErrorCode.MYDATA_TRANSACTION_SYNC_FAILED, exception.getCode());
        verifyNoInteractions(financialSnapshotCreateService);
    }

    @Test
    void 스냅샷_생성에_실패하면_예외를_전파한다() {
        List<TransactionVO> candidates = List.of();
        given(mydataTransactionImportService.importTransactions(candidates))
            .willReturn(new MydataTransactionImportService.SyncResult(0, 0));
        given(mydataConnectionMapper.updateLastSyncedAt(
            eq(101L), eq("MOCK"), any(LocalDateTime.class)
        )).willReturn(1);
        given(mydataDuplicateTransactionService.excludeLikelyAccountDuplicates(101L)).willReturn(0);
        given(fixedExpenseCandidateService.findCandidates(101L)).willReturn(List.of());
        doThrow(new IllegalStateException("snapshot failed"))
            .when(financialSnapshotCreateService).createSnapshot(101L);

        assertThrows(
            IllegalStateException.class,
            () -> service.persistAndRefreshSnapshot(101L, candidates)
        );
    }

    @Test
    void 스냅샷_생성에_실패하면_Spring_트랜잭션이_롤백된다() {
        List<TransactionVO> candidates = List.of();
        given(mydataTransactionImportService.importTransactions(candidates))
            .willReturn(new MydataTransactionImportService.SyncResult(0, 0));
        given(mydataConnectionMapper.updateLastSyncedAt(
            eq(101L), eq("MOCK"), any(LocalDateTime.class)
        )).willReturn(1);
        given(mydataDuplicateTransactionService.excludeLikelyAccountDuplicates(101L)).willReturn(0);
        given(fixedExpenseCandidateService.findCandidates(101L)).willReturn(List.of());
        doThrow(new IllegalStateException("snapshot failed"))
            .when(financialSnapshotCreateService).createSnapshot(101L);

        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        given(transactionManager.getTransaction(any())).willReturn(transactionStatus);

        assertThrows(
            IllegalStateException.class,
            () -> transactionalProxy(transactionManager)
                .persistAndRefreshSnapshot(101L, candidates)
        );

        verify(transactionManager).rollback(transactionStatus);
    }

    private MydataSyncPersistenceService transactionalProxy(
        PlatformTransactionManager transactionManager
    ) {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(service);
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addAdvice(new TransactionInterceptor(
            transactionManager,
            new AnnotationTransactionAttributeSource()
        ));
        return (MydataSyncPersistenceService) proxyFactory.getProxy();
    }
}
