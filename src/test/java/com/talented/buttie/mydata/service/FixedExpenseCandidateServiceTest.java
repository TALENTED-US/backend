package com.talented.buttie.mydata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.ledger.domain.TransactionSource;
import com.talented.buttie.ledger.domain.TransactionType;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import com.talented.buttie.mydata.dto.response.FixedExpenseCandidateResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FixedExpenseCandidateServiceTest {

    @Mock
    private TransactionMapper transactionMapper;
    @Mock
    private PKCrypto pkCrypto;
    @InjectMocks
    private FixedExpenseCandidateService service;

    @Test
    void 두달_이상_반복된_구독결제를_고정지출_후보로_반환한다() {
        TransactionVO june = subscription(1L, 11L, 6, 17_000, TransactionType.EXPENSE);
        TransactionVO july = subscription(2L, 11L, 7, 17_000, TransactionType.EXPENSE);
        TransactionVO august = subscription(3L, 11L, 8, 17_000, TransactionType.EXPENSE);
        given(transactionMapper.findExternalTransactionsForAnalysis(101L))
            .willReturn(List.of(june, july, august));
        given(pkCrypto.encryptValue(3L)).willReturn("encrypted-3");

        List<FixedExpenseCandidateResponse> result = service.findCandidates(101L);

        assertEquals(1, result.size());
        assertEquals("넷플릭스", result.get(0).transactionContent());
        assertEquals(3, result.get(0).occurrenceCount());
        assertEquals("encrypted-3", result.get(0).representativeTransactionId());
    }

    @Test
    void 서로_다른_카드의_동일_가맹점_거래를_별도_후보로_반환한다() {
        List<TransactionVO> transactions = List.of(
            subscription(1L, 11L, 6, 17_000, TransactionType.EXPENSE),
            subscription(2L, 11L, 7, 17_000, TransactionType.EXPENSE),
            subscription(3L, 22L, 6, 25_000, TransactionType.EXPENSE),
            subscription(4L, 22L, 7, 25_000, TransactionType.EXPENSE)
        );
        given(transactionMapper.findExternalTransactionsForAnalysis(101L))
            .willReturn(transactions);
        given(pkCrypto.encryptValue(2L)).willReturn("encrypted-2");
        given(pkCrypto.encryptValue(4L)).willReturn("encrypted-4");

        List<FixedExpenseCandidateResponse> result = service.findCandidates(101L);

        assertEquals(2, result.size());
        assertEquals(25_000, result.get(0).expectedAmount());
        assertEquals(17_000, result.get(1).expectedAmount());
    }

    @Test
    void 이미_고정지출로_확정한_자산_그룹은_후보에서_제외한다() {
        List<TransactionVO> transactions = List.of(
            subscription(1L, 11L, 6, 17_000, TransactionType.EXPENSE),
            subscription(2L, 11L, 7, 17_000, TransactionType.EXPENSE),
            subscription(3L, 11L, 8, 17_000, TransactionType.FIXED)
        );
        given(transactionMapper.findExternalTransactionsForAnalysis(101L))
            .willReturn(transactions);

        List<FixedExpenseCandidateResponse> result = service.findCandidates(101L);

        assertEquals(0, result.size());
    }

    private TransactionVO subscription(
        Long id,
        Long cardId,
        int month,
        int amount,
        TransactionType transactionType
    ) {
        return TransactionVO.builder()
            .transactionId(id)
            .cardId(cardId)
            .transactionSource(TransactionSource.CARD)
            .transactionType(transactionType)
            .expenseCategory(ExpenseCategory.SUBSCRIPTION)
            .transactionContent("넷플릭스")
            .transactionAmount(amount)
            .transactionAt(LocalDateTime.of(2026, month, 14, 10, 0))
            .analysisExcluded(false)
            .build();
    }
}
