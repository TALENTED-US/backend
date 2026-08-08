package com.talented.buttie.ledger.domain.calendar;

import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.ledger.domain.TransactionType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarTransactionVO {

    private Long transactionId;

    private LocalDateTime transactionAt;

    private String transactionContent;

    private ExpenseCategory category;

    private TransactionType transactionType;

    private int amount;

    private String institutionName;
}
