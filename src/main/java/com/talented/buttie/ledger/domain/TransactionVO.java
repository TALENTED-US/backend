package com.talented.buttie.ledger.domain;

import com.talented.buttie.ledger.dto.request.CreateTransactionRequest;
import com.talented.buttie.ledger.dto.request.UpdateTransactionMemoRequest;
import com.talented.buttie.ledger.dto.request.UpdateTransactionRequest;
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
    private String externalTransactionId;
    private String transactionContent;
    private TransactionType transactionType;
    private ExpenseCategory expenseCategory;
    private Integer transactionAmount;
    private LocalDateTime transactionAt;
    private String transactionMemo;
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
}
