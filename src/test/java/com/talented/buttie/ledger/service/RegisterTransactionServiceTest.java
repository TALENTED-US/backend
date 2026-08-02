package com.talented.buttie.ledger.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.ledger.domain.TransactionType;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.dto.request.RegisterTransactionRequestDTO;
import com.talented.buttie.ledger.exception.LedgerErrorCode;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterTransactionServiceTest {
    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private RegisterTransactionService registerTransactionService;

    private Long userId;
    private RegisterTransactionRequestDTO request;

    @BeforeEach
    void setUp(){
        userId = 1L;
        request = RegisterTransactionRequestDTO.builder()
            .type(TransactionType.EXPENSE)
            .amount(10000)
            .category(ExpenseCategory.FOOD)
            .transactionDate(LocalDateTime.parse("2026-07-28T00:00:00"))
            .memo("학식당에서 스팸치즈순두부찌개")
            .build();
    }

    @Test
    @DisplayName("거래 수동 추가 성공")
    void registerTransaction(){
        given(transactionMapper.insertTransaction(any(TransactionVO.class))).willReturn(1);

        TransactionVO result = registerTransactionService.registerTransaction(userId, request);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(10000, result.getTransactionAmount());
        assertEquals("학식당에서 스팸치즈순두부찌개", result.getTransactionMemo());

        verify(transactionMapper).insertTransaction(any(TransactionVO.class));

    }

    @Test
    @DisplayName("요청이 null인 경우 예외가 발생한다.")
    void whenRequestNull(){
        ApplicationException exception = assertThrows(ApplicationException.class, ()-> registerTransactionService.registerTransaction(userId, null));

        assertEquals(LedgerErrorCode.TRANSACTION_BAD_REQUEST.getMessage(), exception.getMessage());
        assertEquals(LedgerErrorCode.TRANSACTION_BAD_REQUEST, exception.getCode());
    }

}