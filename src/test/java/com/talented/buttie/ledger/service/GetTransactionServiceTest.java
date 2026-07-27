package com.talented.buttie.ledger.service;

import static org.junit.jupiter.api.Assertions.*;

import com.talented.buttie.ledger.mapper.TransactionMapper;
import org.junit.jupiter.api.BeforeEach;
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
}