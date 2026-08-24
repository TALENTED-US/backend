package com.talented.buttie.mydata.dto.response;

import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.ledger.domain.TransactionSource;

public record FixedExpenseCandidateResponse(
    String representativeTransactionId,
    String transactionContent,
    String expenseCategory,
    TransactionSource transactionSource,
    int expectedAmount,
    int expectedPaymentDay,
    int occurrenceCount
) {
}
