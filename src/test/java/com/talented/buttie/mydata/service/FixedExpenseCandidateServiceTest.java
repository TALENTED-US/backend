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
        TransactionVO june = subscription(1L, 6, 17_000);
        TransactionVO july = subscription(2L, 7, 17_000);
        TransactionVO august = subscription(3L, 8, 17_000);
        given(transactionMapper.findExternalTransactionsForAnalysis(101L))
            .willReturn(List.of(june, july, august));
        given(pkCrypto.encryptValue(3L)).willReturn("encrypted-3");

        List<FixedExpenseCandidateResponse> result = service.findCandidates(101L);

        assertEquals(1, result.size());
        assertEquals("넷플릭스", result.get(0).transactionContent());
        assertEquals(3, result.get(0).occurrenceCount());
        assertEquals("encrypted-3", result.get(0).representativeTransactionId());
    }

    private TransactionVO subscription(Long id, int month, int amount) {
        return TransactionVO.builder()
            .transactionId(id)
            .transactionSource(TransactionSource.CARD)
            .transactionType(TransactionType.EXPENSE)
            .expenseCategory(ExpenseCategory.SUBSCRIPTION)
            .transactionContent("넷플릭스")
            .transactionAmount(amount)
            .transactionAt(LocalDateTime.of(2026, month, 14, 10, 0))
            .analysisExcluded(false)
            .build();
    }
}
