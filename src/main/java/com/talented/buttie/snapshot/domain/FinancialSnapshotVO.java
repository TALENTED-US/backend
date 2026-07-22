package com.talented.buttie.snapshot.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FinancialSnapshotVO {
    private Long snapshotId;
    private Long userId;
    private LocalDate baseDate;
    private Integer calculationMonths;
    private Integer totalAssets;
    private Integer liquidAssets;
    private Integer financialProductAssets;
    private Integer totalDebt;
    private Integer avgMonthlyIncome;
    private Integer avgMonthlyExpense;
    private Integer monthlyNetCashflow;
    private BigDecimal prepPossibleMonths;
    private Integer targetBalance;
    private Integer additionalRequiredAmount;
    private BigDecimal targetAchievementRate;
    private RiskLevel riskLevel;
    private LocalDateTime createdAt;
}
