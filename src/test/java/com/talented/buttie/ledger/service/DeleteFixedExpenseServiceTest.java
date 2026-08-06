package com.talented.buttie.ledger.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.TransactionType;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.exception.LedgerErrorCode;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import com.talented.buttie.ledger.service.fixed.DeleteFixedExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteFixedExpenseServiceTest {

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private DeleteFixedExpenseService deleteFixedExpenseService;

    private Long userId;
    private Long transactionId;

    @BeforeEach
    void setUp() {
        userId = 1L;
        transactionId = 100L;
    }

    @Test
    @DisplayName("성공: 고정 지출 거래를 일반 지출로 해제(삭제)")
    void deleteFixedExpenseSuccess() {
        TransactionVO fixedTransaction = TransactionVO.builder()
            .transactionId(transactionId)
            .userId(userId)
            .transactionType(TransactionType.FIXED)
            .build();

        given(transactionMapper.findById(transactionId)).willReturn(fixedTransaction);
        given(transactionMapper.updateTransaction(any(TransactionVO.class))).willReturn(1);

        Long result = deleteFixedExpenseService.deleteFixedExpense(userId, transactionId);

        assertEquals(transactionId, result);
        verify(transactionMapper).updateTransaction(any(TransactionVO.class));
    }

    @Test
    @DisplayName("실패: 거래가 존재하지 않는 경우 예외 발생")
    void whenTransactionNotFound() {
        given(transactionMapper.findById(transactionId)).willReturn(null);

        ApplicationException exception = assertThrows(ApplicationException.class,
            () -> deleteFixedExpenseService.deleteFixedExpense(userId, transactionId));

        assertEquals(LedgerErrorCode.TRANSACTION_NOT_FOUND, exception.getCode());
        verify(transactionMapper, never()).updateTransaction(any());
    }

    @Test
    @DisplayName("실패: 다른 사용자의 거래인 경우 예외 발생")
    void whenUserIdMismatch() {
        TransactionVO otherUserTransaction = TransactionVO.builder()
            .transactionId(transactionId)
            .userId(999L)
            .transactionType(TransactionType.FIXED)
            .build();

        given(transactionMapper.findById(transactionId)).willReturn(otherUserTransaction);

        ApplicationException exception = assertThrows(ApplicationException.class,
            () -> deleteFixedExpenseService.deleteFixedExpense(userId, transactionId));

        assertEquals(LedgerErrorCode.TRANSACTION_USER_ID_MISMATCH, exception.getCode());
        verify(transactionMapper, never()).updateTransaction(any());
    }

    @Test
    @DisplayName("실패: 고정 지출이 아닌 일반 거래를 고정지출 해제하려는 경우 예외 발생")
    void whenNotFixedExpense() {
        TransactionVO normalExpense = TransactionVO.builder()
            .transactionId(transactionId)
            .userId(userId)
            .transactionType(TransactionType.EXPENSE)
            .build();

        given(transactionMapper.findById(transactionId)).willReturn(normalExpense);

        ApplicationException exception = assertThrows(ApplicationException.class,
            () -> deleteFixedExpenseService.deleteFixedExpense(userId, transactionId));

        assertEquals(LedgerErrorCode.NOT_FIXED_EXPENSE, exception.getCode());
        verify(transactionMapper, never()).updateTransaction(any());
    }
}
