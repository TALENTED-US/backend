package com.talented.buttie.ledger.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.dto.request.UpdateTransactionMemoRequest;
import com.talented.buttie.ledger.dto.request.UpdateTransactionRequest;
import com.talented.buttie.ledger.exception.LedgerErrorCode;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    @DisplayName("성공: 외부거래 ID가 없는 수동 거래 전체 수정 (PATCH /api/transactions/{id})")
    void updateTransaction() {
        Long userId = 1L;
        Long transactionId = 100L;

        TransactionVO existingTransaction = TransactionVO.builder()
            .transactionId(transactionId)
            .userId(userId)
            .externalTransactionId(null)
            .transactionAmount(1000)
            .expenseCategory(ExpenseCategory.FOOD)
            .transactionAt(LocalDateTime.of(2026,7,1,12,0))
            .transactionMemo("수동 거래 메모")
            .build();

        UpdateTransactionRequest request = UpdateTransactionRequest.builder()
            .transactionAmount(10000)
            .expenseCategory(ExpenseCategory.FOOD)
            .transactionDate(LocalDateTime.parse("2026-07-28T15:30:00"))
            .transactionMemo("변경 시도")
            .build();

        given(transactionMapper.findById(transactionId)).willReturn(existingTransaction);
        given(transactionMapper.updateTransaction(any(TransactionVO.class))).willReturn(1);

        TransactionVO result = updateTransactionService.updateTransaction(userId, transactionId, request);

        assertEquals(10000, result.getTransactionAmount());
        assertEquals("변경 시도", result.getTransactionMemo());
        assertEquals(LocalDateTime.of(2026, 7, 28, 15, 30), result.getTransactionAt());
        verify(transactionMapper, times(1)).updateTransaction(any(TransactionVO.class));
    }

    @Test
    @DisplayName("성공: 외부거래 ID가 있는 거래의 메모 단일 수정 (PATCH /api/transactions/{id}/memo)")
    void updateTransactionMemo() {
        Long userId = 1L;
        Long transactionId = 100L;
        UpdateTransactionMemoRequest memoRequest = new UpdateTransactionMemoRequest("외부 연동 거래 수정된 메모");

        TransactionVO existingTransaction = TransactionVO.builder()
            .transactionId(transactionId)
            .userId(userId)
            .externalTransactionId("EXT-9999")
            .transactionAmount(15000)
            .transactionMemo("원래 메모")
            .build();

        given(transactionMapper.findById(transactionId)).willReturn(existingTransaction);
        given(transactionMapper.updateTransactionMemo(transactionId, "외부 연동 거래 수정된 메모")).willReturn(1);

        TransactionVO result = updateTransactionService.updateTransactionMemo(userId, transactionId, memoRequest);

        assertEquals("외부 연동 거래 수정된 메모", result.getTransactionMemo());
        assertEquals(15000, result.getTransactionAmount());
        verify(transactionMapper, times(1)).updateTransactionMemo(transactionId, "외부 연동 거래 수정된 메모");
    }

    @Test
    @DisplayName("실패: 외부거래 ID가 있는 거래를 PATCH /api/transactions/{id}로 수정 시 예외 발생")
    void whenExternalTransactionFullUpdate(){
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

        UpdateTransactionRequest request = UpdateTransactionRequest.builder()
            .transactionAmount(5000)
            .expenseCategory(ExpenseCategory.FOOD)
            .transactionDate(LocalDateTime.parse("2026-07-01T12:00:00"))
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
        UpdateTransactionRequest request = UpdateTransactionRequest.builder()
            .transactionAmount(10000)
            .expenseCategory(ExpenseCategory.FOOD)
            .transactionDate(LocalDateTime.parse("2026-07-28T00:00:00"))
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