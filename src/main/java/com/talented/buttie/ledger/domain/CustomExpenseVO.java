package com.talented.buttie.ledger.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CustomExpenseVO {
    private Long customExpenseId;
    private Long userId;
    private Boolean isFixed;
    private String name;
    private String category;
    private Integer amount;
    private LocalDate expectedDate;
    private Boolean isRecurring;
    private RecurrenceType recurrenceType;
    private LocalDate recurrenceEndDate;
    private CustomExpenseStatus status;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
