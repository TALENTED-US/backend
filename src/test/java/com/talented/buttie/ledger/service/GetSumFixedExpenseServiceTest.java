package com.talented.buttie.ledger.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.ledger.domain.TransactionType;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.exception.LedgerErrorCode;
import com.talented.buttie.ledger.mapper.TransactionMapper;
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
class GetSumFixedExpenseServiceTest {

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private GetSumFixedExpenseService getSumFixedExpenseService;

    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 1L;
    }

    @Test
    @DisplayName("성공: 고정 지출 내역 목록 조회")
    void getFixedExpensesSuccess() {
        TransactionVO fixed1 = TransactionVO.builder()
            .transactionId(101L)
            .userId(userId)
            .transactionAmount(500000)
            .expenseCategory(ExpenseCategory.HOUSING)
            .transactionType(TransactionType.FIXED)
            .build();

        TransactionVO fixed2 = TransactionVO.builder()
            .transactionId(102L)
            .userId(userId)
            .transactionAmount(60000)
            .expenseCategory(ExpenseCategory.COMMUNICATION)
            .transactionType(TransactionType.FIXED)
            .build();

        given(transactionMapper.findFixedExpensesByUserId(userId)).willReturn(List.of(fixed1, fixed2));

        List<TransactionVO> fixedExpenses = getSumFixedExpenseService.getFixedExpenses(userId);

        assertNotNull(fixedExpenses);
        assertEquals(2, fixedExpenses.size());
    }

    @Test
    @DisplayName("실패: 고정 지출 내역이 없는 경우 FIXED_EXPENSE_NOT_FOUND 예외 발생")
    void whenNoFixedExpensesThrowException() {
        given(transactionMapper.findFixedExpensesByUserId(userId)).willReturn(Collections.emptyList());

        ApplicationException exception = assertThrows(ApplicationException.class,
            () -> getSumFixedExpenseService.getFixedExpenses(userId));

        assertEquals(LedgerErrorCode.FIXED_EXPENSE_NOT_FOUND, exception.getCode());
    }
}
