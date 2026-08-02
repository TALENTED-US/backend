package com.talented.buttie.ledger.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.exception.LedgerErrorCode;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetTransactionServiceTest {

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private GetTransactionService getTransactionService;

    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 1L;
    }

    @Test
    @DisplayName("거래 목록 조회")
    void getAllTransactions(){
            TransactionVO mockTransaction1 = TransactionVO.builder()
                .userId(userId)
                .transactionContent("세종대학교 학식당")
                .transactionAmount(10000)
                .build();

            TransactionVO mockTransaction2 = TransactionVO.builder()
                .userId(userId)
                .transactionContent("스타벅스")
                .transactionAmount(4500)
                .build();

            List<TransactionVO> mockList = List.of(mockTransaction1, mockTransaction2);

            given(transactionMapper.findAllByUserId(userId)).willReturn(mockList);

            List<TransactionVO> result = getTransactionService.getAllTransactions(userId);

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("세종대학교 학식당", result.get(0).getTransactionContent());

            verify(transactionMapper).findAllByUserId(userId);
    }

    @Test
    @DisplayName("사용자 거래목록이 없을때 예외가 발생한다.")
    void ThrowsWhenNull(){
        given(transactionMapper.findAllByUserId(userId)).willReturn(null);

        ApplicationException exception = assertThrows(ApplicationException.class, () -> getTransactionService.getAllTransactions(userId));

        assertEquals(LedgerErrorCode.TRANSACTION_NOT_FOUND.getMessage(), exception.getMessage());
        assertEquals(LedgerErrorCode.TRANSACTION_NOT_FOUND, exception.getCode());

        verify(transactionMapper).findAllByUserId(userId);
    }
}
