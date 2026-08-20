package com.talented.buttie.ledger.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.ClassificationMethod;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.ledger.domain.TransactionSource;
import com.talented.buttie.ledger.domain.TransactionType;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.dto.request.transaction.ClassifyAccountTransactionRequest;
import com.talented.buttie.ledger.exception.LedgerErrorCode;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import com.talented.buttie.ledger.service.transaction.ClassifyAccountTransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClassifyAccountTransactionServiceTest {

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private ClassifyAccountTransactionService classifyAccountTransactionService;

    @Test
    void 계좌이체를_사용자_확정_고정지출로_등록한다() {
        TransactionVO transfer = TransactionVO.builder()
            .transactionId(10001L)
            .userId(1L)
            .accountId(10L)
            .transactionSource(TransactionSource.ACCOUNT)
            .transactionType(TransactionType.TRANSFER)
            .analysisExcluded(true)
            .build();
        ClassifyAccountTransactionRequest request = ClassifyAccountTransactionRequest.builder()
            .transactionType(TransactionType.FIXED)
            .expenseCategory(ExpenseCategory.HOUSING_COMMUNICATION)
            .build();

        given(transactionMapper.findById(10001L)).willReturn(transfer);
        given(transactionMapper.updateTransactionClassification(any(TransactionVO.class))).willReturn(1);

        TransactionVO result = classifyAccountTransactionService.classify(1L, 10001L, request);

        assertEquals(TransactionType.FIXED, result.getTransactionType());
        assertEquals(ExpenseCategory.HOUSING_COMMUNICATION, result.getExpenseCategory());
        assertEquals(ClassificationMethod.USER_CONFIRMED, result.getClassificationMethod());
        assertFalse(result.getAnalysisExcluded());
    }

    @Test
    void 카드_거래는_사용자_계좌이체_분류_대상이_아니다() {
        TransactionVO cardExpense = TransactionVO.builder()
            .transactionId(10L)
            .userId(1L)
            .transactionSource(TransactionSource.CARD)
            .transactionType(TransactionType.EXPENSE)
            .build();
        ClassifyAccountTransactionRequest request = ClassifyAccountTransactionRequest.builder()
            .transactionType(TransactionType.FIXED)
            .expenseCategory(ExpenseCategory.HOBBY_LEISURE)
            .build();

        given(transactionMapper.findById(10L)).willReturn(cardExpense);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> classifyAccountTransactionService.classify(1L, 10L, request)
        );

        assertEquals(LedgerErrorCode.ACCOUNT_TRANSFER_REQUIRED, exception.getCode());
        verify(transactionMapper, never()).updateTransactionClassification(any());
    }

    @Test
    void 계좌이체를_수입으로_등록할_수_없다() {
        TransactionVO transfer = TransactionVO.builder()
            .transactionId(10001L)
            .userId(1L)
            .transactionSource(TransactionSource.ACCOUNT)
            .transactionType(TransactionType.TRANSFER)
            .build();
        ClassifyAccountTransactionRequest request = ClassifyAccountTransactionRequest.builder()
            .transactionType(TransactionType.INCOME)
            .expenseCategory(ExpenseCategory.HOUSING_COMMUNICATION)
            .build();

        given(transactionMapper.findById(10001L)).willReturn(transfer);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> classifyAccountTransactionService.classify(1L, 10001L, request)
        );

        assertEquals(LedgerErrorCode.INVALID_EXPENSE_CLASSIFICATION_TYPE, exception.getCode());
        verify(transactionMapper, never()).updateTransactionClassification(any());
    }
}
