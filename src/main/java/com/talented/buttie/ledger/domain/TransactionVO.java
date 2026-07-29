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
    private TransactionType transactionType;
    private ExpenseCategory category;
    private Integer amount;
    private LocalDateTime transactionAt;
    private String transactionMemo;
    private Boolean analysisExcluded;
    private Boolean isDeleted;
    private LocalDateTime transactionCreatedAt;
    private LocalDateTime transactionUpdatedAt;
}
