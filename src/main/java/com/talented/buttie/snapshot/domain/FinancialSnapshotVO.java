package com.talented.buttie.snapshot.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FinancialSnapshotVO {
    private Long snapshotId;
    private Long userId;
    private LocalDate snapshotBaseDate;
    private Integer liquidAssets;
    private Integer monthlyNetCashflow;
    private BigDecimal prepPossibleMonths;
    private BigDecimal avgWeekendExpense;
    private BigDecimal avgWeekendIncome;
    private BigDecimal avgWeekExpense;
    private BigDecimal avgWeekIncome;
    private RiskLevel riskLevel;
    private LocalDateTime snapshotCreatedAt;
}
