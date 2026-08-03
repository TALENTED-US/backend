package com.talented.buttie.ledger.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.exception.LedgerErrorCode;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteTransactionServiceTest {

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private DeleteTransactionService deleteTransactionService;

    private Long userId;
    private Long transactionId;

    @BeforeEach
    void setUp() {
        userId = 1L;
        transactionId = 100L;
    }

    @Test
    @DisplayName("성공: 수동 등록 거래 삭제")
    void deleteTransactionSuccess() {
        TransactionVO existingTransaction = TransactionVO.builder()
            .transactionId(transactionId)
            .userId(userId)
            .externalTransactionId(null)
            .build();

        given(transactionMapper.findById(transactionId)).willReturn(existingTransaction);
        given(transactionMapper.deleteTransaction(transactionId)).willReturn(1);

        Long result = deleteTransactionService.deleteTransaction(userId, transactionId);

        assertEquals(userId, result);
        verify(transactionMapper).deleteTransaction(transactionId);
    }

    @Test
    @DisplayName("실패: 거래가 존재하지 않을 때 예외 발생")
    void whenTransactionNotFound() {
        given(transactionMapper.findById(transactionId)).willReturn(null);

        ApplicationException exception = assertThrows(ApplicationException.class,
            () -> deleteTransactionService.deleteTransaction(userId, transactionId));

        assertEquals(LedgerErrorCode.TRANSACTION_NOT_FOUND, exception.getCode());
        verify(transactionMapper, never()).deleteTransaction(anyLong());
    }

    @Test
    @DisplayName("실패: 다른 사용자의 거래 삭제 시도 시 예외 발생")
    void whenUserIdMismatch() {
        TransactionVO otherUserTransaction = TransactionVO.builder()
            .transactionId(transactionId)
            .userId(999L)
            .externalTransactionId(null)
            .build();

        given(transactionMapper.findById(transactionId)).willReturn(otherUserTransaction);

        ApplicationException exception = assertThrows(ApplicationException.class,
            () -> deleteTransactionService.deleteTransaction(userId, transactionId));

        assertEquals(LedgerErrorCode.TRANSACTION_DELETE_USER_ID_MISMATCH, exception.getCode());
        verify(transactionMapper, never()).deleteTransaction(anyLong());
    }

    @Test
    @DisplayName("실패: 외부 연동 거래 삭제 시도 시 예외 발생")
    void whenExternalTransactionDelete() {
        TransactionVO externalTransaction = TransactionVO.builder()
            .transactionId(transactionId)
            .userId(userId)
            .externalTransactionId("EXT-1234")
            .build();

        given(transactionMapper.findById(transactionId)).willReturn(externalTransaction);

        ApplicationException exception = assertThrows(ApplicationException.class,
            () -> deleteTransactionService.deleteTransaction(userId, transactionId));

        assertEquals(LedgerErrorCode.EXTERNAL_TRANSACTION_UNDELETABLE, exception.getCode());
        verify(transactionMapper, never()).deleteTransaction(anyLong());
    }
}
