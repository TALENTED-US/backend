package com.talented.buttie.mydata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.talented.buttie.ledger.domain.TransactionSource;
import com.talented.buttie.ledger.domain.TransactionType;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import com.talented.buttie.mydata.service.mydata.MydataDuplicateTransactionService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MydataDuplicateTransactionServiceTest {

    @Mock
    private TransactionMapper transactionMapper;
    @InjectMocks
    private MydataDuplicateTransactionService service;

    @Test
    void 금액_시각_카드적요가_일치하면_계좌거래만_분석에서_제외한다() {
        TransactionVO account = TransactionVO.builder()
            .transactionId(1L)
            .transactionSource(TransactionSource.ACCOUNT)
            .transactionType(TransactionType.TRANSFER)
            .transactionAmount(12_800)
            .transactionAt(LocalDateTime.of(2026, 8, 22, 18, 55))
            .transactionMemo("체크카드 스타벅스")
            .analysisExcluded(false)
            .build();
        TransactionVO card = TransactionVO.builder()
            .transactionId(2L)
            .transactionSource(TransactionSource.CARD)
            .transactionType(TransactionType.EXPENSE)
            .transactionAmount(12_800)
            .transactionAt(LocalDateTime.of(2026, 8, 22, 18, 51))
            .merchantName("스타벅스")
            .build();
        given(transactionMapper.findExternalTransactionsForAnalysis(101L))
            .willReturn(List.of(account, card));
        given(transactionMapper.updateAnalysisExcluded(1L, true)).willReturn(1);

        int result = service.excludeLikelyAccountDuplicates(101L);

        assertEquals(1, result);
        verify(transactionMapper).updateAnalysisExcluded(1L, true);
    }

    @Test
    void 카드_중복이_아닌_미분류_계좌이체도_분석에서_제외한다() {
        TransactionVO account = TransactionVO.builder()
            .transactionId(1L)
            .transactionSource(TransactionSource.ACCOUNT)
            .transactionType(TransactionType.TRANSFER)
            .transactionAmount(50_000)
            .transactionAt(LocalDateTime.of(2026, 8, 22, 18, 55))
            .transactionMemo("친구 모임 정산")
            .analysisExcluded(false)
            .build();
        given(transactionMapper.findExternalTransactionsForAnalysis(101L))
            .willReturn(List.of(account));
        given(transactionMapper.updateAnalysisExcluded(1L, true)).willReturn(1);

        int duplicateCount = service.excludeLikelyAccountDuplicates(101L);

        assertEquals(0, duplicateCount);
        verify(transactionMapper).updateAnalysisExcluded(1L, true);
    }
}
