package com.talented.buttie.simulation.dto.response;

import com.talented.buttie.simulation.domain.SimulationItemCategory;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

@Builder
public record PreviewItemResponseDTO(
    List<MonthlyBalancePreviewDTO> monthlyBalances,
    CashFlowPreviewDTO cashflow,
    ItemEffectPreviewDTO itemEffect
) {
    @Builder
    public record MonthlyBalancePreviewDTO(
        LocalDate projectionMonth,
        Integer beforeClosingBalance,
        Integer afterClosingBalance,
        Integer balanceDelta
    ) {}

    @Builder
    public record CashFlowPreviewDTO(
       Integer beforeMonthlyIncome,
       Integer afterMonthlyIncome,
       Integer incomeDelta,
       Integer beforeMonthlyExpense,
       Integer afterMonthlyExpense,
       Integer expenseDelta,
       Integer beforeMonthlyNetCashFlow,
       Integer afterMonthlyNetCashFlow,
       Integer netCashFlowDelta
    ) {}

    @Builder
    public record ItemEffectPreviewDTO(
        String itemName,
        SimulationItemCategory category,
        Integer monthlyEffectAmount,
        Integer onceEffectAmount
    ) {}
}
