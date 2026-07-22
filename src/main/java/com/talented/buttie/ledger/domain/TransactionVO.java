package com.talented.buttie.ledger.domain;

import java.time.LocalDateTime;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransactionVO {
    private Long transactionId;
    private Long userId;
    private Long accountId;
    private String externalTransactionId;
    private String content;
    private Boolean isExpense;
    private ExpenseCategory category;
    private Integer amount;
    private LocalDateTime transactionAt;
    private String memo;
    private Boolean analysisExcluded;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
