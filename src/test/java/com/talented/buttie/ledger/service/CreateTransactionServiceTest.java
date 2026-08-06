package com.talented.buttie.ledger.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.ledger.domain.TransactionType;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.dto.request.transaction.CreateTransactionRequest;
import com.talented.buttie.ledger.exception.LedgerErrorCode;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import com.talented.buttie.ledger.service.transaction.CreateTransactionService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateTransactionServiceTest {
    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private CreateTransactionService createTransactionService;

    private Long userId;
    private CreateTransactionRequest request;

    @BeforeEach
    void setUp(){
        userId = 1L;
        request = CreateTransactionRequest.builder()
            .transactionContent("세종대학교 학식당")
            .transactionType(TransactionType.EXPENSE)
            .transactionAmount(10000)
            .expenseCategory(ExpenseCategory.FOOD)
            .transactionDate(LocalDateTime.parse("2026-07-28T00:00:00"))
            .transactionMemo("학식당에서 스팸치즈순두부찌개")
            .build();
    }

    @Test
    @DisplayName("거래 수동 추가 성공")
    void createTransaction(){
        given(transactionMapper.insertTransaction(any(TransactionVO.class))).willAnswer(invocation -> {
            TransactionVO vo = invocation.getArgument(0);
            vo.setTransactionId(100L);
            return 1;
        });

        Long result = createTransactionService.createTransaction(userId, request);

        assertNotNull(result);
        assertEquals(100L, result);

        verify(transactionMapper).insertTransaction(any(TransactionVO.class));
    }

    @Test
    @DisplayName("요청이 null인 경우 예외가 발생한다.")
    void whenRequestNull(){
        ApplicationException exception = assertThrows(ApplicationException.class, ()-> createTransactionService.createTransaction(userId, null));

        assertEquals(LedgerErrorCode.TRANSACTION_BAD_REQUEST.getMessage(), exception.getMessage());
        assertEquals(LedgerErrorCode.TRANSACTION_BAD_REQUEST, exception.getCode());
    }

}