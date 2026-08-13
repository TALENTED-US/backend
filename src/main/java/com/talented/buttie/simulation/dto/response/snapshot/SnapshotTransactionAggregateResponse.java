package com.talented.buttie.simulation.dto.response.snapshot;

import java.math.BigDecimal;

public record SnapshotTransactionAggregateResponse(
    BigDecimal weekIncomeTotal,
    BigDecimal weekExpenseTotal,
    BigDecimal weekendIncomeTotal,
    BigDecimal weekendExpenseTotal,
    BigDecimal monthlyIncomeTotal,
    BigDecimal monthlyExpenseTotal
) {

    public static SnapshotTransactionAggregateResponse empty() {
        return new SnapshotTransactionAggregateResponse(
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO, BigDecimal.ZERO
        );
    }

    public BigDecimal weekIncomeTotalOrZero() {
        return valueOrZero(weekIncomeTotal);
    }

    public BigDecimal weekExpenseTotalOrZero() {
        return valueOrZero(weekExpenseTotal);
    }

    public BigDecimal weekendIncomeTotalOrZero() {
        return valueOrZero(weekendIncomeTotal);
    }

    public BigDecimal weekendExpenseTotalOrZero() {
        return valueOrZero(weekendExpenseTotal);
    }

    public BigDecimal monthlyIncomeTotalOrZero() {
        return valueOrZero(monthlyIncomeTotal);
    }

    public BigDecimal monthlyExpenseTotalOrZero() {
        return valueOrZero(monthlyExpenseTotal);
    }

    private static BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

}
