package com.talented.buttie.ledger.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.dto.response.TransactionDetailResponse;
import com.talented.buttie.ledger.exception.LedgerErrorCode;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReadTransactionDetailServiceTest {

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private ReadTransactionDetailService readTransactionDetailService;

    private Long userId;
    private Long transactionId;
    private TransactionVO sampleTransaction;

    @BeforeEach
    void setUp() {
        userId = 1L;
        transactionId = 100L;
        sampleTransaction = TransactionVO.builder()
            .transactionId(transactionId)
            .userId(userId)
            .transactionContent("점심 식사")
            .transactionAt(LocalDateTime.of(2026, 8, 3, 12, 30, 0))
            .transactionMemo("강남역 맛집")
            .transactionAmount(12000)
            .build();
    }

    @Test
    @DisplayName("성공: 거래 상세 조회")
    void getTransactionDetailSuccess() {
        given(transactionMapper.findById(transactionId)).willReturn(sampleTransaction);

        TransactionDetailResponse response = readTransactionDetailService.getTransactionDetail(userId, transactionId);

        assertNotNull(response);
        assertEquals("점심 식사", response.transactionContent());
        assertEquals(LocalDateTime.of(2026, 8, 3, 12, 30, 0), response.transactionAt());
        assertEquals("강남역 맛집", response.transactionMemo());
        assertEquals(12000, response.transactionAmount());
    }

    @Test
    @DisplayName("실패: 거래 정보가 없을 때 예외 발생")
    void whenTransactionNotFound() {
        given(transactionMapper.findById(transactionId)).willReturn(null);

        ApplicationException exception = assertThrows(ApplicationException.class,
            () -> readTransactionDetailService.getTransactionDetail(userId, transactionId));

        assertEquals(LedgerErrorCode.TRANSACTION_NOT_FOUND, exception.getCode());
    }

    @Test
    @DisplayName("실패: 다른 사용자의 거래 조회 시 권한 예외 발생")
    void whenUserIdMismatch() {
        TransactionVO otherUserTransaction = sampleTransaction.toBuilder()
            .userId(999L)
            .build();

        given(transactionMapper.findById(transactionId)).willReturn(otherUserTransaction);

        ApplicationException exception = assertThrows(ApplicationException.class,
            () -> readTransactionDetailService.getTransactionDetail(userId, transactionId));

        assertEquals(LedgerErrorCode.TRANSACTION_USER_ID_MISMATCH, exception.getCode());
    }
}
