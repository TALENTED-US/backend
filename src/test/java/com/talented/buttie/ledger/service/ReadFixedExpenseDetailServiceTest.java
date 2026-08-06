package com.talented.buttie.ledger.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.ledger.domain.TransactionType;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.exception.LedgerErrorCode;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import com.talented.buttie.ledger.service.fixed.ReadFixedExpenseDetailService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReadFixedExpenseDetailServiceTest {

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private ReadFixedExpenseDetailService readFixedExpenseDetailService;

    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 1L;
    }

    @Test
    @DisplayName("성공: 고정 지출 상세 목록 조회")
    void getFixedExpenseDetailsSuccess() {
        TransactionVO fixedTransaction1 = TransactionVO.builder()
            .transactionId(101L)
            .userId(userId)
            .transactionContent("월세")
            .transactionAmount(500000)
            .expenseCategory(ExpenseCategory.HOUSING)
            .transactionType(TransactionType.FIXED)
            .build();

        TransactionVO fixedTransaction2 = TransactionVO.builder()
            .transactionId(102L)
            .userId(userId)
            .transactionContent("통신비")
            .transactionAmount(60000)
            .expenseCategory(ExpenseCategory.COMMUNICATION)
            .transactionType(TransactionType.FIXED)
            .build();

        given(transactionMapper.findFixedExpensesByUserId(userId))
            .willReturn(List.of(fixedTransaction1, fixedTransaction2));

        List<TransactionVO> result = readFixedExpenseDetailService.getFixedExpenseDetails(userId);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(101L, result.get(0).getTransactionId());
        assertEquals("월세", result.get(0).getTransactionContent());
        assertEquals(500000, result.get(0).getTransactionAmount());
        assertEquals(ExpenseCategory.HOUSING, result.get(0).getExpenseCategory());

        assertEquals(102L, result.get(1).getTransactionId());
        assertEquals("통신비", result.get(1).getTransactionContent());
        assertEquals(60000, result.get(1).getTransactionAmount());
        assertEquals(ExpenseCategory.COMMUNICATION, result.get(1).getExpenseCategory());
    }

    @Test
    @DisplayName("실패: 고정 지출 내역이 없을 경우 예외 발생")
    void whenNoFixedExpensesThrowException() {
        given(transactionMapper.findFixedExpensesByUserId(userId)).willReturn(Collections.emptyList());

        ApplicationException exception = assertThrows(ApplicationException.class,
            () -> readFixedExpenseDetailService.getFixedExpenseDetails(userId));

        assertEquals(LedgerErrorCode.FIXED_EXPENSE_NOT_FOUND, exception.getCode());
    }
}
