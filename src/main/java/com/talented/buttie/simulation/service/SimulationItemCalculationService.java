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
import com.talented.buttie.simulation.dto.response.PreviewItemResponse;
import com.talented.buttie.simulation.dto.response.PreviewItemResponse.CashFlowPreview;
import com.talented.buttie.simulation.dto.response.PreviewItemResponse.ItemEffectPreview;
import com.talented.buttie.simulation.dto.response.PreviewItemResponse.MonthlyBalancePreview;
import com.talented.buttie.simulation.exception.SimulationErrorCode;
import com.talented.buttie.snapshot.domain.FinancialSnapshotVO;
import com.talented.buttie.user.mapper.EmploymentPreparationMapper;
import java.math.BigDecimal;
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

    public PreviewItemResponse createPreviewResponse(
        SimulationItemVO targetItem,
        List<MonthlyProjectionVO> beforeProjections,
        List<MonthlyProjectionVO> afterProjections
    ) {
        List<MonthlyProjectionVO> sortedBefore = beforeProjections.stream()
            .sorted(Comparator.comparing(MonthlyProjectionVO::getProjectionMonth))
            .toList();

        List<MonthlyProjectionVO> sortedAfter = afterProjections.stream()
            .sorted(Comparator.comparing(MonthlyProjectionVO::getProjectionMonth))
            .toList();

        List<MonthlyBalancePreview> monthlyBalances = sortedAfter.stream()
            .map(after -> {
                MonthlyProjectionVO before = findProjectionByMonth(sortedBefore, after.getProjectionMonth());

                return MonthlyBalancePreview.builder()
                    .projectionMonth(after.getProjectionMonth())
                    .beforeClosingBalance(before.getClosingBalance())
                    .afterClosingBalance(after.getClosingBalance())
                    .balanceDelta(valueOf(after.getClosingBalance()) - valueOf(before.getClosingBalance()))
                    .build();
            })
            .toList();

        MonthlyProjectionVO beforeFirst = sortedBefore.get(0);
        MonthlyProjectionVO afterFirst = sortedAfter.get(0);

        int beforeMonthlyIncome = valueOf(beforeFirst.getExpectedIncome());
        int beforeMonthlyExpense = valueOf(beforeFirst.getExpectedExpense());
        int afterMonthlyIncome = valueOf(afterFirst.getExpectedIncome());
        int afterMonthlyExpense = valueOf(afterFirst.getExpectedExpense());

        int effectAmount = valueOf(targetItem.getSimulationItemApplyAmount());
        int monthlyEffectAmount = targetItem.getRecurrenceType() == SimulationRecurrenceType.MONTHLY ? effectAmount : 0;
        int onceEffectAmount = targetItem.getRecurrenceType() == SimulationRecurrenceType.ONCE ? effectAmount : 0;

        return PreviewItemResponse.builder()
            .monthlyBalances(monthlyBalances)
            .cashflow(
                CashFlowPreview.builder()
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
            .itemEffect(
                ItemEffectPreview.builder()
                    .itemName(targetItem.getSimulationItemName())
                    .category(targetItem.getSimulationItemCategory())
                    .monthlyEffectAmount(monthlyEffectAmount)
                    .onceEffectAmount(onceEffectAmount)
                    .build()
            )
            .build();
    }

    public BigDecimal calculatePrepMonths(List<MonthlyProjectionVO> projections) {
        long positiveMonths = projections.stream()
            .filter(projection -> valueOf(projection.getClosingBalance()) >= 0)
            .count();

        return BigDecimal.valueOf(positiveMonths);
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
            return toKoreanExpenseCategoryName(request.expenseCategory()) + " 줄이기";
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

    private String toKoreanExpenseCategoryName(ExpenseCategory expenseCategory) {
        return switch (expenseCategory) {
            case FOOD -> "식비";
            case TRANSPORT -> "교통비";
            case HOUSING -> "주거비";
            case COMMUNICATION -> "통신비";
            case SUBSCRIPTION -> "구독비";
            case EDUCATION -> "교육비";
            case CERTIFICATE -> "자격증 비용";
            case ETC_EXPENSE -> "기타 비용";
        };
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
}
