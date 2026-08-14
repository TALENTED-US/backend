package com.talented.buttie.ledger.domain;

import com.talented.buttie.ledger.dto.request.transaction.CreateTransactionRequest;
import com.talented.buttie.ledger.dto.request.transaction.UpdateTransactionMemoRequest;
import com.talented.buttie.ledger.dto.request.transaction.UpdateTransactionRequest;
import java.time.LocalDateTime;
import lombok.*;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TransactionVO {
    private Long transactionId;
    private Long userId;
    private Long accountId;
    private Long cardId;
    private String externalTransactionId;
    private TransactionSource transactionSource;
    private ClassificationMethod classificationMethod;
    private String merchantName;
    private String merchantRegistrationNumber;
    private String transactionContent;
    private TransactionType transactionType;
    private ExpenseCategory expenseCategory;
    private Integer transactionAmount;
    private LocalDateTime transactionAt;
    private String transactionMemo;
    private Boolean analysisExcluded;
    private Boolean isDeleted;
    private LocalDateTime transactionCreatedAt;
    private LocalDateTime transactionUpdatedAt;

    public static TransactionVO createTransaction(
        Long userId,
        CreateTransactionRequest request
    ){
        return TransactionVO.builder()
            .userId(userId)
            .transactionContent(request.transactionContent())
            .transactionType(request.transactionType())
            .transactionAmount(request.transactionAmount())
            .expenseCategory(request.expenseCategory())
            .transactionAt(request.transactionDate())
            .transactionMemo(request.transactionMemo())
            .transactionSource(TransactionSource.MANUAL)
            .classificationMethod(ClassificationMethod.MANUAL)
            .analysisExcluded(false)
            .build();
    }

    public static TransactionVO updateTransaction(
        TransactionVO original,
        UpdateTransactionRequest request
    ){
        return original.toBuilder()
            .transactionAmount(request.transactionAmount() != null ? request.transactionAmount() : original.getTransactionAmount())
            .expenseCategory(request.expenseCategory() != null ? request.expenseCategory() : original.getExpenseCategory())
            .transactionAt(request.transactionDate() != null ? request.transactionDate() : original.getTransactionAt())
            .transactionMemo(request.transactionMemo())
            .build();
    }

    public static TransactionVO updateTransactionMemo(
        TransactionVO original,
        UpdateTransactionMemoRequest request
    ){
        return original.toBuilder()
            .transactionMemo(request.memo())
            .build();
    }

    public static TransactionVO createFixedExpense(
        TransactionVO transaction
    ){
        return transaction.toBuilder()
            .transactionType(TransactionType.FIXED)
            .classificationMethod(ClassificationMethod.USER_CONFIRMED)
            .analysisExcluded(false)
            .build();
    }

    public static TransactionVO deleteFixedExpense(
        TransactionVO transaction
    ){
        return transaction.toBuilder()
            .transactionType(TransactionType.EXPENSE)
            .analysisExcluded(false)
            .build();
    }

    public static TransactionVO classifyAccountExpense(
        TransactionVO transaction,
        TransactionType transactionType,
        ExpenseCategory expenseCategory
    ) {
        return transaction.toBuilder()
            .transactionType(transactionType)
            .expenseCategory(expenseCategory)
            .classificationMethod(ClassificationMethod.USER_CONFIRMED)
            .analysisExcluded(false)
            .build();
    }
}
