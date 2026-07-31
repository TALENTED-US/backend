package com.talented.buttie.ledger.service;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.dto.request.UpdateTransactionRequestDTO;
import com.talented.buttie.ledger.exception.LedgerErrorCode;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateTransactionServiceTest {
    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private UpdateTransactionService updateTransactionService;

    @Test
    @DisplayName("성공: 수동 추가된 거래 내역 전체 수정")
    void updateTransaction() {
        Long userId = 1L;
        Long transactionId = 100L;

        TransactionVO existingTransaction = TransactionVO.builder()
            .transactionId(transactionId)
            .userId(userId)
            .externalTransactionId("EXT-9999")
            .transactionAmount(1000)
            .expenseCategory(ExpenseCategory.FOOD)
            .transactionAt(LocalDateTime.of(2026,7,1,12,0))
            .transactionMemo("외부 거래 메모")
            .build();

        UpdateTransactionRequestDTO request = UpdateTransactionRequestDTO.builder()
            .transactionAmount(10000)
            .expenseCategory(ExpenseCategory.FOOD)
            .transactionDate(LocalDateTime.of(2026,7,28,15,30))
            .transactionMemo("변경 시도")
            .build();

        given(transactionMapper.findById(transactionId)).willReturn(existingTransaction);
        given(transactionMapper.updateTransaction(any(TransactionVO.class))).willReturn(1);

        TransactionVO result = updateTransactionService.updateTransaction(userId, transactionId, request);

        assertEquals(10000, result.getTransactionAmount());
        assertEquals("수정된 메모", result.getTransactionMemo());
        verify(transactionMapper, times(1)).updateTransaction(any(TransactionVO.class));
    }

    @Test
    @DisplayName("거래 메모 단일 수정")
    void updateMemo() {
        Long userId = 1L;
        Long transactionId = 100L;
        String newMemo = "단일 수정된 메모입니다.";

        TransactionVO existingTransaction = TransactionVO.builder()
            .transactionId(transactionId)
            .userId(userId)
            .externalTransactionId("EXT-9999")
            .transactionAmount(15000)
            .transactionMemo("원래 메모")
            .build();

        given(transactionMapper.findById(transactionId)).willReturn(existingTransaction);
        given(transactionMapper.updateTransaction(any(TransactionVO.class))).willReturn(1);

        TransactionVO result = updateTransactionService.updateMemo(userId, transactionId, newMemo);

        assertEquals(newMemo, result.getTransactionMemo());
        assertEquals(15000, result.getTransactionAmount());
        verify(transactionMapper, times(1)).updateTransaction(any(TransactionVO.class));

    }

    @Test
    @DisplayName("외부 연동 거래를 수동으로 수정하려고하면 예외 발생")
    void whenExternalTransaction(){
        Long userId = 1L;
        Long transactionId = 100L;

        TransactionVO existingTransaction = TransactionVO.builder()
            .transactionId(transactionId)
            .userId(userId)
            .externalTransactionId("EXT-9999")
            .transactionAmount(5000)
            .expenseCategory(ExpenseCategory.FOOD)
            .transactionAt(LocalDateTime.of(2026,7,1,12,0))
            .transactionMemo("외부 거래 메모")
            .build();

        UpdateTransactionRequestDTO request = UpdateTransactionRequestDTO.builder()
            .transactionAmount(10000)
            .expenseCategory(ExpenseCategory.FOOD)
            .transactionDate(LocalDateTime.of(2026,7,28,15,30))
            .transactionMemo("변경 시도")
            .build();

        given(transactionMapper.findById(transactionId)).willReturn(existingTransaction);

        ApplicationException exception = assertThrows(
            ApplicationException.class, () -> updateTransactionService.updateTransaction(userId, transactionId, request)
        );

        assertEquals(LedgerErrorCode.EXTERNAL_TRANSACTION_UNMODIFIABLE, exception.getCode());
        verify(transactionMapper, times(0)).updateTransaction(any(TransactionVO.class));
    }

    @Test
    @DisplayName("존재하지 않는 ID 거나 타인의 ID일때 예외 발생")
    void whenNotFound(){
        Long userId = 1L;
        Long transactionId = 999L;
        UpdateTransactionRequestDTO request = UpdateTransactionRequestDTO.builder()
            .transactionAmount(10000)
            .expenseCategory(ExpenseCategory.FOOD)
            .transactionDate(LocalDateTime.now())
            .transactionMemo("테스트")
            .build();

        given(transactionMapper.findById(transactionId)).willReturn(null);

        ApplicationException exception = assertThrows(
            ApplicationException.class, ()-> updateTransactionService.updateTransaction(userId, transactionId, request)
        );

        assertEquals(LedgerErrorCode.TRANSACTION_NOT_FOUND, exception.getCode());
        verify(transactionMapper, times(0)).updateTransaction(any(TransactionVO.class));
    }
}