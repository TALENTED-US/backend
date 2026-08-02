package com.talented.buttie.ledger.domain;

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
}
