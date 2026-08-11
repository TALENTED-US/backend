package com.talented.buttie.simulation.service;

import com.talented.buttie.catalog.domain.PolicyStatus;
import com.talented.buttie.catalog.domain.PolicyVO;
import com.talented.buttie.catalog.exception.CatalogErrorCode;
import com.talented.buttie.catalog.mapper.PolicyMapper;
import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.common.util.PKCrypto;
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
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SimulationItemCalculationService {

    private final EmploymentPreparationMapper employmentPreparationMapper;
    private final PolicyMapper policyMapper;
    private final ProjectionEngine projectionEngine;

    private static final BigDecimal MAX_EXPECT_PREP_MONTHS = new BigDecimal("999.99");

    public void validateRequest(ApplySimulationItemRequest request, SimulationVO simulation) {
        if (request.category() == null || request.applyStartDate() == null) {
            throw ApplicationException.from(SimulationErrorCode.INVALID_SIMULATION_ITEM);
        }

        LocalDate applyEndDate = request.category() == SimulationItemCategory.POLICY
            ? request.applyStartDate()
            : resolveApplyEndDate(request);

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

        PolicyVO policy = policyMapper.findById(PKCrypto.decrypt(request.policyId()));

        if (policy == null) {
            throw ApplicationException.from(CatalogErrorCode.POLICY_NOT_FOUND);
        }

        if (policy.getPolicyStatus() == PolicyStatus.CLOSED) {
            throw ApplicationException.from(CatalogErrorCode.POLICY_NOT_AVAILABLE);
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
            .policyId(request.category() == SimulationItemCategory.POLICY
                ? policy.getPolicyId()
                : null)
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
        Map<Long, PolicyVO> policyById = appliedItems.stream()
            .filter(item -> item.getPolicyId() != null)
            .collect(Collectors.toMap(SimulationItemVO::getPolicyId, SimulationItemVO::getPolicy,
                (a, b) -> a));

        List<MonthlyProjectionVO> baselineProjections = createBaselineProjections(simulation, snapshot);

        List<MonthlyProjectionVO> appliedProjections = baselineProjections.stream()
            .map(projection -> applyItemsToProjection(projection, appliedItems, simulation, policyById))
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
                    .belowLivingFundThreshold(after.getClosingBalance() < livingFundThreshold)
                    .build();
            })
            .toList();

        MonthlyProjectionVO beforeFirst = sortedBefore.get(0);
        MonthlyProjectionVO afterFirst = sortedAfter.get(0);

        int beforeMonthlyIncome = valueOf(beforeFirst.getExpectedIncome());
        int beforeMonthlyExpense = valueOf(beforeFirst.getExpectedExpense());
        int afterMonthlyIncome = valueOf(afterFirst.getExpectedIncome());
        int afterMonthlyExpense = valueOf(afterFirst.getExpectedExpense());
        PrepMonthsCalculation expectedPrepMonths = calculateExpectedPrepMonthsResult(sortedAfter, snapshot);

        return SimulationItemReportResponse.builder()
            .currentPrepMonths(snapshot.getCurrentPrepMonths())
            .expectPrepMonths(expectedPrepMonths.months())
            .currentSustainable(isCurrentSustainable(snapshot))
            .expectSustainable(expectedPrepMonths.sustainable())
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
            PolicyVO policy = item.getPolicy();
            boolean monthly = resolveItemRecurrenceType(item, policy) == SimulationRecurrenceType.MONTHLY;

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
        List<MonthlyProjectionVO> projections,
        FinancialSnapshotVO snapshot
    ) {
        return calculateExpectedPrepMonthsResult(projections, snapshot).months();
    }

    private PrepMonthsCalculation calculateExpectedPrepMonthsResult(
        List<MonthlyProjectionVO> projections,
        FinancialSnapshotVO snapshot
    ) {
        if(projections == null || projections.isEmpty()) {
            return new PrepMonthsCalculation(snapshot.getCurrentPrepMonths(), isCurrentSustainable(snapshot));
        }

        List<MonthlyProjectionVO> sortedProjections = projections.stream()
            .sorted(Comparator.comparing(MonthlyProjectionVO::getProjectionMonth))
            .toList();

        BigDecimal currentMonthlyBurn = calculateCurrentMonthlyBurn(snapshot);
        boolean neverDepletesDuringSimulation = sortedProjections.stream()
            .allMatch(projection -> valueOf(projection.getExpectedExpense())
                <= valueOf(projection.getExpectedIncome()));

        if (neverDepletesDuringSimulation
            && currentMonthlyBurn != null
            && currentMonthlyBurn.compareTo(BigDecimal.ZERO) <= 0) {
            return new PrepMonthsCalculation(MAX_EXPECT_PREP_MONTHS, true);
        }

        // 잔액이 마이너스로 떨어져도 시뮬레이션 끝까지 계속 진행시켜, 이후 수입으로
        // 회복되는 경우(예: 다음 달에 목돈 유입)를 생존 기간에 반영한다.
        BigDecimal openingBalance = BigDecimal.valueOf(valueOf(sortedProjections.get(0).getOpeningBalance()));
        List<BigDecimal> closingBalances = new ArrayList<>();
        BigDecimal balance = openingBalance;

        for (MonthlyProjectionVO projection : sortedProjections) {
            BigDecimal monthlyIncome = BigDecimal.valueOf(valueOf(projection.getExpectedIncome()));
            BigDecimal monthlyExpense = BigDecimal.valueOf(valueOf(projection.getExpectedExpense()));
            BigDecimal monthlyBurn = monthlyExpense.subtract(monthlyIncome);
            balance = balance.subtract(monthlyBurn);
            closingBalances.add(balance);
        }

        BigDecimal finalBalance = closingBalances.get(closingBalances.size() - 1);

        if (finalBalance.compareTo(BigDecimal.ZERO) >= 0) {
            if (currentMonthlyBurn == null || currentMonthlyBurn.compareTo(BigDecimal.ZERO) <= 0) {
                return new PrepMonthsCalculation(MAX_EXPECT_PREP_MONTHS, true);
            }

            BigDecimal additionalMonths = finalBalance
                .divide(currentMonthlyBurn, 2, RoundingMode.HALF_UP);

            return new PrepMonthsCalculation(
                capPrepMonths(BigDecimal.valueOf(sortedProjections.size()).add(additionalMonths)),
                false
            );
        }

        // 시뮬레이션 종료 시점까지도 마이너스라면, 다시는 회복되지 않는
        // 마지막 마이너스 구간의 시작점을 실질적인 고갈 시점으로 계산한다.
        int depletionMonthIndex = closingBalances.size() - 1;
        while (depletionMonthIndex > 0
            && closingBalances.get(depletionMonthIndex - 1).compareTo(BigDecimal.ZERO) < 0) {
            depletionMonthIndex--;
        }

        BigDecimal balanceBeforeDepletionMonth = depletionMonthIndex == 0
            ? openingBalance
            : closingBalances.get(depletionMonthIndex - 1);

        MonthlyProjectionVO depletionProjection = sortedProjections.get(depletionMonthIndex);
        BigDecimal depletionMonthlyBurn = BigDecimal.valueOf(valueOf(depletionProjection.getExpectedExpense()))
            .subtract(BigDecimal.valueOf(valueOf(depletionProjection.getExpectedIncome())));

        BigDecimal partialMonth = depletionMonthlyBurn.compareTo(BigDecimal.ZERO) <= 0
            ? BigDecimal.ONE
            : balanceBeforeDepletionMonth.max(BigDecimal.ZERO)
                .divide(depletionMonthlyBurn, 2, RoundingMode.HALF_UP)
                .min(BigDecimal.ONE);

        return new PrepMonthsCalculation(
            capPrepMonths(BigDecimal.valueOf(depletionMonthIndex).add(partialMonth)),
            false
        );
    }

    private boolean isCurrentSustainable(FinancialSnapshotVO snapshot) {
        BigDecimal monthlyBurn = calculateCurrentMonthlyBurn(snapshot);
        return monthlyBurn != null && monthlyBurn.compareTo(BigDecimal.ZERO) <= 0;
    }

    private record PrepMonthsCalculation(BigDecimal months, boolean sustainable) {}

    private BigDecimal capPrepMonths(BigDecimal prepMonths) {
        return prepMonths.compareTo(MAX_EXPECT_PREP_MONTHS) > 0
            ? MAX_EXPECT_PREP_MONTHS
            : prepMonths;
    }


    private BigDecimal calculateCurrentMonthlyBurn(FinancialSnapshotVO snapshot) {
        if (snapshot == null) {
            return null;
        }

        return decimalValueOf(snapshot.getAvgMonthlyExpense())
            .subtract(decimalValueOf(snapshot.getAvgMonthlyIncome()))
            .setScale(2, RoundingMode.HALF_UP);
    }

    private MonthlyProjectionVO applyItemsToProjection(
        MonthlyProjectionVO projection,
        List<SimulationItemVO> appliedItems,
        SimulationVO simulation,
        Map<Long, PolicyVO> policyById
    ) {
        int expectedIncome = valueOf(projection.getExpectedIncome());
        int expectedExpense = valueOf(projection.getExpectedExpense());

        for (SimulationItemVO item : appliedItems) {
            int monthlyEffect = calculateMonthlyEffect(
                item,
                projection.getProjectionMonth(),
                simulation,
                policyById.get(item.getPolicyId())
            );

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

    private int calculateMonthlyEffect(
        SimulationItemVO item,
        LocalDate projectionMonth,
        SimulationVO simulation,
        PolicyVO policy
    ) {
        YearMonth targetMonth = YearMonth.from(projectionMonth);
        LocalDate applyStartDate = item.getApplyStartDate();
        LocalDate applyEndDate = resolveItemApplyEndDate(item, simulation, policy);

        YearMonth applyStartMonth = YearMonth.from(applyStartDate);
        YearMonth applyEndMonth = YearMonth.from(applyEndDate);

        if (targetMonth.isBefore(applyStartMonth) || targetMonth.isAfter(applyEndMonth)) {
            return 0;
        }

        SimulationRecurrenceType recurrenceType = resolveItemRecurrenceType(item, policy);

        if (recurrenceType == SimulationRecurrenceType.MONTHLY) {
            int recurrenceDay = applyStartDate.getDayOfMonth();
            int day = Math.min(recurrenceDay, targetMonth.lengthOfMonth());
            LocalDate effectDate = targetMonth.atDay(day);

            if (effectDate.isBefore(applyStartDate)
                || effectDate.isAfter(applyEndDate)
                || effectDate.isBefore(simulation.getSimulationStartDate())) {
                return 0;
            }

            return valueOf(item.getSimulationItemApplyAmount());
        }

        if (recurrenceType == SimulationRecurrenceType.ONCE) {
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
        if (request.category() == SimulationItemCategory.POLICY) {
            return null;
        }

        return request.applyEndDate() == null ? request.applyStartDate() : request.applyEndDate();
    }

    private LocalDate resolveItemApplyEndDate(
        SimulationItemVO item,
        SimulationVO simulation,
        PolicyVO policy
    ) {
        LocalDate applyEndDate;
        if (item.getSimulationItemCategory() == SimulationItemCategory.POLICY) {
            int supportMonthCount = policy == null || policy.getSupportMonthCount() == null
                ? 1
                : Math.max(policy.getSupportMonthCount(), 1);
            applyEndDate = item.getApplyStartDate().plusMonths(supportMonthCount - 1L);
        } else {
            applyEndDate = item.getApplyEndDate() == null
                ? item.getApplyStartDate()
                : item.getApplyEndDate();
        }

        return applyEndDate.isAfter(simulation.getSimulationDueDate())
            ? simulation.getSimulationDueDate()
            : applyEndDate;
    }

    private SimulationRecurrenceType resolveRecurrenceType(ApplySimulationItemRequest request, PolicyVO policy) {
        if (request.category() == SimulationItemCategory.POLICY) {
            int supportMonthCount = policy.getSupportMonthCount() == null
                ? 1
                : Math.max(policy.getSupportMonthCount(), 1);

            return supportMonthCount == 1
                ? SimulationRecurrenceType.ONCE
                : SimulationRecurrenceType.MONTHLY;
        }

        return request.recurrenceType();
    }

    private SimulationRecurrenceType resolveItemRecurrenceType(SimulationItemVO item, PolicyVO policy) {
        if (item.getSimulationItemCategory() != SimulationItemCategory.POLICY) {
            return item.getRecurrenceType();
        }

        int supportMonthCount = policy == null || policy.getSupportMonthCount() == null
            ? 1
            : Math.max(policy.getSupportMonthCount(), 1);

        return supportMonthCount == 1
            ? SimulationRecurrenceType.ONCE
            : SimulationRecurrenceType.MONTHLY;
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

}
