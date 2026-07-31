package com.talented.buttie.simulation.service;

import com.talented.buttie.simulation.domain.MonthlyProjectionVO;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ProjectionEngine {

    public List<MonthlyProjectionVO> recalculateProjections(
        Long simulationId,
        LocalDate simulationStartDate,
        LocalDate simulationDueDate,
        List<MonthlyProjectionVO> existingProjections
    ) {
        validate(
            simulationStartDate,
            simulationDueDate,
            existingProjections
        );

        List<MonthlyProjectionVO> sortedProjections = existingProjections.stream()
            .sorted(Comparator.comparing(MonthlyProjectionVO::getProjectionMonth))
            .toList();

        MonthlyProjectionVO defaultProjection = sortedProjections.get(0);

        Map<YearMonth, MonthlyProjectionVO> projectionByMonth =
            sortedProjections.stream()
                .collect(Collectors.toMap(
                    projection -> YearMonth.from(projection.getProjectionMonth()),
                    Function.identity(),
                    (first, second) -> first
                ));

        List<MonthlyProjectionVO> recalculatedProjections = new ArrayList<>();
        YearMonth currentMonth = YearMonth.from(simulationStartDate);
        YearMonth dueMonth = YearMonth.from(simulationDueDate);

        MonthlyProjectionVO startingProjection = projectionByMonth.getOrDefault(currentMonth, defaultProjection);
        int currentBalance = startingProjection.getOpeningBalance();

        while (!currentMonth.isAfter(dueMonth)) {
            MonthlyProjectionVO monthlyCondition =
                projectionByMonth.getOrDefault(currentMonth, defaultProjection);

            int expectedIncome = monthlyCondition.getExpectedIncome();
            int expectedExpense = monthlyCondition.getExpectedExpense();
            int closingBalance =
                currentBalance + expectedIncome - expectedExpense;
            boolean adjustmentRequired = closingBalance < 0;

            recalculatedProjections.add(MonthlyProjectionVO.builder()
                .simulationId(simulationId)
                .projectionMonth(currentMonth.atDay(1))
                .openingBalance(currentBalance)
                .expectedIncome(expectedIncome)
                .expectedExpense(expectedExpense)
                .closingBalance(closingBalance)
                .adjustmentRequired(adjustmentRequired)
                .adjustmentReason(
                    adjustmentRequired ? "월말 예상 잔액이 부족합니다." : null
                )
                .build());

            currentBalance = closingBalance;
            currentMonth = currentMonth.plusMonths(1);
        }

        return recalculatedProjections;
    }

    private void validate(
        LocalDate simulationStartDate,
        LocalDate simulationDueDate,
        List<MonthlyProjectionVO> existingProjections
    ) {
        if (simulationStartDate == null
            || simulationDueDate == null
            || !simulationDueDate.isAfter(simulationStartDate)) {
            throw new IllegalArgumentException(
                "시뮬레이션 기간이 올바르지 않습니다."
            );
        }

        if (existingProjections == null || existingProjections.isEmpty()) {
            throw new IllegalArgumentException(
                "기존 예상 재정 계획이 없습니다."
            );
        }
    }
}
