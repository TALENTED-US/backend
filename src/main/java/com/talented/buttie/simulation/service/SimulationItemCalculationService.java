package com.talented.buttie.simulation.service;

import com.talented.buttie.catalog.domain.PolicyVO;
import com.talented.buttie.catalog.exception.CatalogErrorCode;
import com.talented.buttie.catalog.mapper.PolicyMapper;
import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.simulation.domain.MonthlyProjectionVO;
import com.talented.buttie.simulation.domain.SimulationItemCategory;
import com.talented.buttie.simulation.domain.SimulationItemVO;
import com.talented.buttie.simulation.domain.SimulationRecurrenceType;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.dto.request.ApplySimulationItemRequest;
import com.talented.buttie.simulation.dto.response.SimulationItemReportResponse;
import com.talented.buttie.simulation.dto.response.SimulationItemReportResponse.CategoryContributionReport;
import com.talented.buttie.simulation.dto.response.SimulationItemReportResponse.CashFlowReport;
import com.talented.buttie.simulation.dto.response.SimulationItemReportResponse.MonthlyBalanceReport;
import com.talented.buttie.simulation.exception.SimulationErrorCode;
import com.talented.buttie.snapshot.domain.FinancialSnapshotVO;
import com.talented.buttie.user.mapper.EmploymentPreparationMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SimulationItemCalculationService {

    private final EmploymentPreparationMapper employmentPreparationMapper;
    private final PolicyMapper policyMapper;
    private final ProjectionEngine projectionEngine;

    public void validateRequest(ApplySimulationItemRequest request, SimulationVO simulation) {
        if (request.category() == null || request.applyStartDate() == null) {
            throw ApplicationException.from(SimulationErrorCode.INVALID_SIMULATION_ITEM);
        }

        LocalDate applyEndDate = resolveApplyEndDate(request);

        if (applyEndDate.isBefore(request.applyStartDate())) {
            throw ApplicationException.from(SimulationErrorCode.INVALID_SIMULATION_ITEM);
        }

        if (request.applyStartDate().isAfter(simulation.getSimulationDueDate())
            || applyEndDate.isBefore(simulation.getSimulationStartDate())) {
            throw ApplicationException.from(SimulationErrorCode.INVALID_SIMULATION_ITEM);
        }

        switch (request.category()) {
            case INCOME -> {
                if (isBlank(request.itemName())
                    || isInvalidAmount(request.amount())
                    || request.recurrenceType() == null) {
                    throw ApplicationException.from(SimulationErrorCode.INVALID_SIMULATION_ITEM);
                }
            }
            case EXPENSE -> {
                if (request.expenseCategory() == null
                    || isInvalidAmount(request.amount())
                    || request.recurrenceType() == null) {
                    throw ApplicationException.from(SimulationErrorCode.INVALID_SIMULATION_ITEM);
                }
            }
            case POLICY -> {
                if (request.policyId() == null) {
                    throw ApplicationException.from(SimulationErrorCode.INVALID_SIMULATION_ITEM);
                }
            }
        }
    }

    public PolicyVO resolvePolicy(ApplySimulationItemRequest request) {
        if (request.category() != SimulationItemCategory.POLICY) {
            return null;
        }

        PolicyVO policy = policyMapper.findById(request.policyId());

        if (policy == null) {
            throw ApplicationException.from(CatalogErrorCode.POLICY_NOT_FOUND);
        }

        return policy;
    }

    public SimulationItemVO createItem(
        SimulationVO simulation,
        ApplySimulationItemRequest request,
        PolicyVO policy
    ) {
        return SimulationItemVO.builder()
            .simulationId(simulation.getSimulationId())
            .simulationItemCategory(request.category())
            .simulationItemName(resolveItemName(request))
            .simulationItemExpenseCategory(resolveExpenseCategory(request))
            .simulationItemApplyAmount(resolveApplyAmount(request, policy))
            .applyStartDate(request.applyStartDate())
            .applyEndDate(resolveApplyEndDate(request))
            .policyId(request.category() == SimulationItemCategory.POLICY ? request.policyId() : null)
            .recurrenceType(resolveRecurrenceType(request, policy))
            .isDeleted(false)
            .build();
    }

    public List<MonthlyProjectionVO> createBaselineProjections(
        SimulationVO simulation,
        FinancialSnapshotVO snapshot
    ) {
        int livingThreshold = valueOf(employmentPreparationMapper.getLivingThresholdByUserId(simulation.getUserId()));

        return projectionEngine.createInitialProjections(
            simulation.getSimulationId(),
            simulation.getSimulationStartDate(),
            simulation.getSimulationDueDate(),
            valueOf(snapshot.getLiquidAssets()),
            intValueOf(snapshot.getAvgMonthlyIncome()),
            intValueOf(snapshot.getAvgMonthlyExpense()),
            livingThreshold
        );
    }

    public List<MonthlyProjectionVO> recalculateProjections(
        SimulationVO simulation,
        FinancialSnapshotVO snapshot,
        List<SimulationItemVO> appliedItems
    ) {
        int livingThreshold = valueOf(employmentPreparationMapper.getLivingThresholdByUserId(simulation.getUserId()));

        List<MonthlyProjectionVO> baselineProjections = createBaselineProjections(simulation, snapshot);

        List<MonthlyProjectionVO> appliedProjections = baselineProjections.stream()
            .map(projection -> applyItemsToProjection(projection, appliedItems))
            .toList();

        return projectionEngine.recalculateProjections(
            simulation.getSimulationId(),
            simulation.getSimulationStartDate(),
            simulation.getSimulationDueDate(),
            appliedProjections,
            livingThreshold
        );
    }

    public SimulationItemReportResponse createReportResponse(
        Long userId,
        FinancialSnapshotVO snapshot,
        List<SimulationItemVO> appliedItems,
        List<MonthlyProjectionVO> beforeProjections,
        List<MonthlyProjectionVO> afterProjections
    ) {
        List<MonthlyProjectionVO> sortedBefore = beforeProjections.stream()
            .sorted(Comparator.comparing(MonthlyProjectionVO::getProjectionMonth))
            .toList();

        List<MonthlyProjectionVO> sortedAfter = afterProjections.stream()
            .sorted(Comparator.comparing(MonthlyProjectionVO::getProjectionMonth))
            .toList();

        int livingFundThreshold = employmentPreparationMapper.getLivingThresholdByUserId(userId);

        List<MonthlyBalanceReport> monthlyBalances = sortedAfter.stream()
            .map(after -> {
                MonthlyProjectionVO before = findProjectionByMonth(sortedBefore, after.getProjectionMonth());

                return MonthlyBalanceReport.builder()
                    .projectionMonth(after.getProjectionMonth())
                    .beforeClosingBalance(before.getClosingBalance())
                    .afterClosingBalance(after.getClosingBalance())
                    .livingFundThreshold(livingFundThreshold)
                    .build();
            })
            .toList();

        MonthlyProjectionVO beforeFirst = sortedBefore.get(0);
        MonthlyProjectionVO afterFirst = sortedAfter.get(0);

        int beforeMonthlyIncome = valueOf(beforeFirst.getExpectedIncome());
        int beforeMonthlyExpense = valueOf(beforeFirst.getExpectedExpense());
        int afterMonthlyIncome = valueOf(afterFirst.getExpectedIncome());
        int afterMonthlyExpense = valueOf(afterFirst.getExpectedExpense());

        return SimulationItemReportResponse.builder()
            .currentPrepMonths(snapshot.getCurrentPrepMonths())
            .expectPrepMonths(calculateExpectedPrepMonths(sortedAfter, snapshot, livingFundThreshold))
            .monthlyBalances(monthlyBalances)
            .cashflow(
                CashFlowReport.builder()
                    .beforeMonthlyIncome(beforeMonthlyIncome)
                    .afterMonthlyIncome(afterMonthlyIncome)
                    .incomeDelta(afterMonthlyIncome - beforeMonthlyIncome)
                    .beforeMonthlyExpense(beforeMonthlyExpense)
                    .afterMonthlyExpense(afterMonthlyExpense)
                    .expenseDelta(afterMonthlyExpense - beforeMonthlyExpense)
                    .beforeMonthlyNetCashFlow(beforeMonthlyIncome - beforeMonthlyExpense)
                    .afterMonthlyNetCashFlow(afterMonthlyIncome - afterMonthlyExpense)
                    .netCashFlowDelta((afterMonthlyIncome - afterMonthlyExpense) - (beforeMonthlyIncome - beforeMonthlyExpense))
                    .build()
            )
            .categoryContribution(calculateCategoryContribution(appliedItems))
            .build();
    }

    private CategoryContributionReport calculateCategoryContribution(List<SimulationItemVO> appliedItems) {
        int expenseMonthlyAmount = 0;
        int expenseOnceAmount = 0;
        int incomeMonthlyAmount = 0;
        int incomeOnceAmount = 0;
        int policyMonthlyAmount = 0;
        int policyOnceAmount = 0;

        for (SimulationItemVO item : appliedItems) {
            int amount = valueOf(item.getSimulationItemApplyAmount());
            boolean monthly = item.getRecurrenceType() == SimulationRecurrenceType.MONTHLY;

            switch (item.getSimulationItemCategory()) {
                case EXPENSE -> {
                    if (monthly) expenseMonthlyAmount += amount;
                    else expenseOnceAmount += amount;
                }
                case INCOME -> {
                    if (monthly) incomeMonthlyAmount += amount;
                    else incomeOnceAmount += amount;
                }
                case POLICY -> {
                    if (monthly) policyMonthlyAmount += amount;
                    else policyOnceAmount += amount;
                }
            }
        }

        return CategoryContributionReport.builder()
            .expenseMonthlyAmount(expenseMonthlyAmount)
            .expenseOnceAmount(expenseOnceAmount)
            .incomeMonthlyAmount(incomeMonthlyAmount)
            .incomeOnceAmount(incomeOnceAmount)
            .policyMonthlyAmount(policyMonthlyAmount)
            .policyOnceAmount(policyOnceAmount)
            .build();
    }

    public BigDecimal calculateExpectedPrepMonths(
        Long userId,
        List<MonthlyProjectionVO> projections,
        FinancialSnapshotVO snapshot
    ) {
        int livingThreshold = valueOf(employmentPreparationMapper.getLivingThresholdByUserId(userId));
        return calculateExpectedPrepMonths(projections, snapshot, livingThreshold);
    }

    public BigDecimal calculateExpectedPrepMonths(
        List<MonthlyProjectionVO> projections,
        FinancialSnapshotVO snapshot,
        Integer livingThreshold
    ) {
        if(projections == null || projections.isEmpty())
            return snapshot.getCurrentPrepMonths();

        List<MonthlyProjectionVO> sortedProjections = projections.stream()
            .sorted(Comparator.comparing(MonthlyProjectionVO::getProjectionMonth))
            .toList();

        BigDecimal survivedMonths = BigDecimal.ZERO;
        BigDecimal balance = BigDecimal.valueOf(valueOf(sortedProjections.get(0).getOpeningBalance()));

        for(MonthlyProjectionVO projection : sortedProjections) {
            BigDecimal monthlyBurn = calculateProjectionMonthlyBurn(projection, livingThreshold);

            if(monthlyBurn.compareTo(BigDecimal.ZERO) <= 0) return null;

            if(balance.compareTo(BigDecimal.ZERO) <= 0) return survivedMonths;

            BigDecimal closingBalance = balance.subtract(monthlyBurn);

            if(closingBalance.compareTo(BigDecimal.ZERO) >= 0) {
                survivedMonths = survivedMonths.add(BigDecimal.ONE);
                balance = closingBalance;
                continue;
            }

            BigDecimal partialMonth = balance
                .divide(monthlyBurn, 2, RoundingMode.HALF_UP);

            if(partialMonth.compareTo(BigDecimal.ONE) > 0) {
                partialMonth = BigDecimal.ONE;
            }

            return survivedMonths.add(partialMonth);
        }

        BigDecimal currentMonthlyBurn = calculateCurrentMonthlyBurn(snapshot, livingThreshold);

        if(currentMonthlyBurn == null || currentMonthlyBurn.compareTo(BigDecimal.ZERO) <= 0) return null;

        BigDecimal additionalMonths = balance
            .divide(currentMonthlyBurn, 2, RoundingMode.HALF_UP);

        return survivedMonths.add(additionalMonths);
    }

    private BigDecimal calculateProjectionMonthlyBurn(MonthlyProjectionVO projection, Integer livingThreshold) {
        BigDecimal requiredMonthlyExpense = BigDecimal.valueOf(valueOf(projection.getExpectedExpense()))
            .max(BigDecimal.valueOf(valueOf(livingThreshold)));

        return requiredMonthlyExpense
            .subtract(BigDecimal.valueOf(valueOf(projection.getExpectedIncome())))
            .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateCurrentMonthlyBurn(FinancialSnapshotVO snapshot, Integer livingThreshold) {
        if (snapshot == null) {
            return null;
        }

        LocalDate today = LocalDate.now();
        int weekDaysInMonth = countDaysInCurrentMonth(today, false);
        int weekendDaysInMonth = countDaysInCurrentMonth(today, true);

        BigDecimal currentMonthlyIncome = decimalValueOf(snapshot.getAvgWeekIncome())
            .multiply(BigDecimal.valueOf(weekDaysInMonth))
            .add(decimalValueOf(snapshot.getAvgWeekendIncome()).multiply(BigDecimal.valueOf(weekendDaysInMonth)));

        BigDecimal currentMonthlyExpense = decimalValueOf(snapshot.getAvgWeekExpense())
            .multiply(BigDecimal.valueOf(weekDaysInMonth))
            .add(decimalValueOf(snapshot.getAvgWeekendExpense()).multiply(BigDecimal.valueOf(weekendDaysInMonth)));

        BigDecimal requiredMonthlyExpense = currentMonthlyExpense
            .max(BigDecimal.valueOf(valueOf(livingThreshold)));

        return requiredMonthlyExpense
            .subtract(currentMonthlyIncome)
            .setScale(2, RoundingMode.HALF_UP);
    }

    private MonthlyProjectionVO applyItemsToProjection(
        MonthlyProjectionVO projection,
        List<SimulationItemVO> appliedItems
    ) {
        int expectedIncome = valueOf(projection.getExpectedIncome());
        int expectedExpense = valueOf(projection.getExpectedExpense());

        for (SimulationItemVO item : appliedItems) {
            int monthlyEffect = calculateMonthlyEffect(item, projection.getProjectionMonth());

            if (item.getSimulationItemCategory() == SimulationItemCategory.EXPENSE) {
                expectedExpense = Math.max(0, expectedExpense - monthlyEffect);
            } else {
                expectedIncome += monthlyEffect;
            }
        }

        return MonthlyProjectionVO.builder()
            .simulationId(projection.getSimulationId())
            .projectionMonth(projection.getProjectionMonth())
            .openingBalance(projection.getOpeningBalance())
            .expectedIncome(expectedIncome)
            .expectedExpense(expectedExpense)
            .closingBalance(projection.getClosingBalance())
            .adjustmentRequired(projection.getAdjustmentRequired())
            .adjustmentReason(projection.getAdjustmentReason())
            .build();
    }

    private int calculateMonthlyEffect(SimulationItemVO item, LocalDate projectionMonth) {
        YearMonth targetMonth = YearMonth.from(projectionMonth);
        LocalDate applyStartDate = item.getApplyStartDate();
        LocalDate applyEndDate = item.getApplyEndDate() == null ? applyStartDate : item.getApplyEndDate();

        YearMonth applyStartMonth = YearMonth.from(applyStartDate);
        YearMonth applyEndMonth = YearMonth.from(applyEndDate);

        if (targetMonth.isBefore(applyStartMonth) || targetMonth.isAfter(applyEndMonth)) {
            return 0;
        }

        if (item.getRecurrenceType() == SimulationRecurrenceType.MONTHLY) {
            int recurrenceDay = applyStartDate.getDayOfMonth();
            int day = Math.min(recurrenceDay, targetMonth.lengthOfMonth());
            LocalDate effectDate = targetMonth.atDay(day);

            if (effectDate.isBefore(applyStartDate) || effectDate.isAfter(applyEndDate)) {
                return 0;
            }

            return valueOf(item.getSimulationItemApplyAmount());
        }

        if (item.getRecurrenceType() == SimulationRecurrenceType.ONCE) {
            return targetMonth.equals(applyStartMonth)
                ? valueOf(item.getSimulationItemApplyAmount())
                : 0;
        }

        return 0;
    }

    private MonthlyProjectionVO findProjectionByMonth(
        List<MonthlyProjectionVO> projections,
        LocalDate projectionMonth
    ) {
        YearMonth targetMonth = YearMonth.from(projectionMonth);

        return projections.stream()
            .filter(projection -> YearMonth.from(projection.getProjectionMonth()).equals(targetMonth))
            .findFirst()
            .orElseThrow(() -> ApplicationException.from(SimulationErrorCode.SIMULATION_PROJECTION_NOT_FOUND));
    }

    private String resolveItemName(ApplySimulationItemRequest request) {
        if (request.category() == SimulationItemCategory.INCOME) {
            return request.itemName();
        }

        if (request.category() == SimulationItemCategory.EXPENSE) {
            return request.expenseCategory().getValue() + " 줄이기";
        }

        return null;
    }

    private ExpenseCategory resolveExpenseCategory(ApplySimulationItemRequest request) {
        if (request.category() == SimulationItemCategory.EXPENSE) {
            return request.expenseCategory();
        }

        return null;
    }

    private int resolveApplyAmount(ApplySimulationItemRequest request, PolicyVO policy) {
        if (request.category() == SimulationItemCategory.POLICY) {
            return valueOf(policy.getPolicySupportAmount());
        }

        return valueOf(request.amount());
    }

    private LocalDate resolveApplyEndDate(ApplySimulationItemRequest request) {
        return request.applyEndDate() == null ? request.applyStartDate() : request.applyEndDate();
    }

    private SimulationRecurrenceType resolveRecurrenceType(ApplySimulationItemRequest request, PolicyVO policy) {
        if (request.category() == SimulationItemCategory.POLICY) {
            return policy.getPolicyRecurrenceType() == null
                ? SimulationRecurrenceType.ONCE
                : policy.getPolicyRecurrenceType();
        }

        return request.recurrenceType();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean isInvalidAmount(Integer amount) {
        return amount == null || amount < 0;
    }

    private int valueOf(Integer value) {
        return value == null ? 0 : value;
    }

    private int intValueOf(BigDecimal value) {
        return value == null ? 0 : value.intValue();
    }

    private BigDecimal decimalValueOf(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private int countDaysInCurrentMonth(LocalDate date, boolean weekend) {
        LocalDate current = date.withDayOfMonth(1);
        LocalDate end = current.plusMonths(1);

        return countDaysInPeriod(current, end, weekend);
    }

    private int countDaysInPeriod(LocalDate fromInclusive, LocalDate toExclusive, boolean weekend) {
        LocalDate current = fromInclusive;
        int count = 0;

        while (current.isBefore(toExclusive)) {
            boolean currentIsWeekend = isWeekend(current.getDayOfWeek());
            if (currentIsWeekend == weekend) {
                count++;
            }
            current = current.plusDays(1);
        }

        return count;
    }

    private boolean isWeekend(DayOfWeek dayOfWeek) {
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
}
