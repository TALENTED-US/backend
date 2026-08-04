package com.talented.buttie.ledger.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.ledger.domain.TransactionType;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.exception.LedgerErrorCode;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateFixedExpenseServiceTest {

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private CreateFixedExpenseService createFixedExpenseService;

    private Long userId;
    private Long transactionId;
    private String encryptedTransactionId;
    private MockedStatic<PKCrypto> pkCryptoMockedStatic;

    @BeforeEach
    void setUp() {
        userId = 1L;
        transactionId = 100L;
        encryptedTransactionId = "enc100";

        pkCryptoMockedStatic = mockStatic(PKCrypto.class);
        pkCryptoMockedStatic.when(() -> PKCrypto.decrypt(encryptedTransactionId)).thenReturn(transactionId);
    }

    @AfterEach
    void tearDown() {
        pkCryptoMockedStatic.close();
    }

    @Test
    @DisplayName("성공: 거래 내역을 고정 지출로 등록")
    void createFixedExpenseSuccess() {
        TransactionVO original = TransactionVO.builder()
            .transactionId(transactionId)
            .userId(userId)
            .transactionType(TransactionType.EXPENSE)
            .build();

        given(transactionMapper.findById(transactionId)).willReturn(original);

        Long result = createFixedExpenseService.createFixedExpense(userId, encryptedTransactionId);

        assertEquals(transactionId, result);
        verify(transactionMapper).updateTransaction(any(TransactionVO.class));
    }

    @Test
    @DisplayName("실패: 거래가 존재하지 않는 경우 예외 발생")
    void whenTransactionNotFound() {
        given(transactionMapper.findById(transactionId)).willReturn(null);

        ApplicationException exception = assertThrows(ApplicationException.class,
            () -> createFixedExpenseService.createFixedExpense(userId, encryptedTransactionId));

        assertEquals(LedgerErrorCode.TRANSACTION_NOT_FOUND, exception.getCode());
        verify(transactionMapper, never()).updateTransaction(any());
    }

    @Test
    @DisplayName("실패: 다른 사용자의 거래인 경우 예외 발생")
    void whenUserIdMismatch() {
        TransactionVO otherUserTransaction = TransactionVO.builder()
            .transactionId(transactionId)
            .userId(999L)
            .transactionType(TransactionType.EXPENSE)
            .build();

        given(transactionMapper.findById(transactionId)).willReturn(otherUserTransaction);

        ApplicationException exception = assertThrows(ApplicationException.class,
            () -> createFixedExpenseService.createFixedExpense(userId, encryptedTransactionId));

        assertEquals(LedgerErrorCode.TRANSACTION_USER_ID_MISMATCH, exception.getCode());
        verify(transactionMapper, never()).updateTransaction(any());
    }

    @Test
    @DisplayName("실패: 이미 고정 지출인 거래를 추가하려는 경우 예외 발생")
    void whenAlreadyFixedExpense() {
        TransactionVO alreadyFixed = TransactionVO.builder()
            .transactionId(transactionId)
            .userId(userId)
            .transactionType(TransactionType.FIXED)
            .build();

        given(transactionMapper.findById(transactionId)).willReturn(alreadyFixed);

        ApplicationException exception = assertThrows(ApplicationException.class,
            () -> createFixedExpenseService.createFixedExpense(userId, encryptedTransactionId));

        assertEquals(LedgerErrorCode.ALREADY_FIXED_EXPENSE, exception.getCode());
        verify(transactionMapper, never()).updateTransaction(any());
    }
}
