package com.talented.buttie.simulation.service;

import com.talented.buttie.catalog.domain.PolicyVO;
import com.talented.buttie.catalog.exception.CatalogErrorCode;
import com.talented.buttie.catalog.mapper.PolicyMapper;
import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.simulation.domain.MonthlyProjectionVO;
import com.talented.buttie.simulation.domain.SimulationItemCategory;
import com.talented.buttie.simulation.domain.SimulationRecurrenceType;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.dto.request.CreateSimulationRequest;
import com.talented.buttie.simulation.dto.request.PreviewItemRequest;
import com.talented.buttie.simulation.dto.response.PreviewItemResponse;
import com.talented.buttie.simulation.dto.response.PreviewItemResponse.CashFlowPreview;
import com.talented.buttie.simulation.dto.response.PreviewItemResponse.ItemEffectPreview;
import com.talented.buttie.simulation.dto.response.PreviewItemResponse.MonthlyBalancePreview;
import com.talented.buttie.simulation.exception.SimulationErrorCode;
import com.talented.buttie.simulation.mapper.MonthlyProjectionMapper;
import com.talented.buttie.simulation.mapper.SimulationMapper;
import com.talented.buttie.snapshot.domain.FinancialSnapshotVO;
import com.talented.buttie.snapshot.service.FinancialSnapshotCreateService;
import com.talented.buttie.user.mapper.EmploymentPreparationMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SimulationCreateService {

    private final FinancialSnapshotCreateService financialSnapshotCreateService;
    private final SimulationMapper simulationMapper;
    private final MonthlyProjectionMapper monthlyProjectionMapper;
    private final ProjectionEngine projectionEngine;
    private final EmploymentPreparationMapper employmentPreparationMapper;
    private final PolicyMapper policyMapper;

    @Transactional
    public SimulationVO createSimulation(Long userId, CreateSimulationRequest request){
        SimulationVO activeSimulation = simulationMapper.findActiveByUserId(userId);

        if(activeSimulation != null) return activeSimulation;

        FinancialSnapshotVO snapshot = financialSnapshotCreateService.createSnapshot(userId);

        SimulationVO simulation = SimulationVO.createCurrentSimulation(userId, request, snapshot);
        simulationMapper.save(simulation);

        Integer livingThreshold = employmentPreparationMapper.getLivingThresholdByUserId(userId);

        List<MonthlyProjectionVO> monthlyProjections = projectionEngine.createInitialProjections(
            simulation.getSimulationId(), simulation.getSimulationStartDate(),
            simulation.getSimulationDueDate(), valueOf(snapshot.getLiquidAssets()),
            intValueOf(snapshot.getAvgMonthlyIncome()), intValueOf(snapshot.getAvgMonthlyExpense()), valueOf(livingThreshold)
        );
        monthlyProjectionMapper.saveAll(monthlyProjections);
        simulation.setMonthlyProjections(monthlyProjections);

        return simulation;
    }

    private int valueOf(Integer value){
        return value == null ? 0 : value;
    }

    private int intValueOf(BigDecimal value) {
        return value == null ? 0 : value.intValue();
    }

    @Transactional(readOnly = true)
    public PreviewItemResponse previewItemResultSimulation(Long userId, PreviewItemRequest request){
        SimulationVO simulation = simulationMapper.findActiveByUserId(userId);

        if(simulation == null){
            throw new ApplicationException(SimulationErrorCode.SIMULATION_NOT_FOUND);
        }

        List<MonthlyProjectionVO> monthlyProjections = monthlyProjectionMapper.findAllBySimulationId(simulation.getSimulationId());

        if(monthlyProjections == null || monthlyProjections.isEmpty()){
            throw ApplicationException.from(SimulationErrorCode.SIMULATION_PROJECTION_NOT_FOUND);
        }

        validatePreviewRequest(request, simulation);

        int effectAmount  = resolveEffectAmount(request);
        List<MonthlyProjectionVO> sortedProjections = monthlyProjections.stream()
            .sorted(Comparator.comparing(MonthlyProjectionVO::getProjectionMonth))
            .toList();

        List<PreviewItemResponse.MonthlyBalancePreview> monthlyBalances = new ArrayList<>();

        int previousAfterClosingBalance = 0;

        for(int idx = 0; idx < sortedProjections.size(); idx++){
            MonthlyProjectionVO projection = sortedProjections.get(idx);

            int beforeIncome = valueOf(projection.getExpectedIncome());
            int beforeExpense = valueOf(projection.getExpectedExpense());
            int afterIncome = beforeIncome;
            int afterExpense = beforeExpense;

            int monthlyEffect = calculateMonthlyEffect(request, projection.getProjectionMonth(), effectAmount);

            if(request.simulationItemCategory() == SimulationItemCategory.EXPENSE){
                afterExpense = Math.max(0, beforeExpense - monthlyEffect);
            }else{
                afterIncome = beforeIncome + monthlyEffect;
            }

            int afterOpeningBalance = idx == 0
                ? valueOf(projection.getOpeningBalance())
                : previousAfterClosingBalance;

            int afterClosingBalance = afterOpeningBalance + afterIncome - afterExpense;

            previousAfterClosingBalance = afterClosingBalance;

            monthlyBalances.add(
                MonthlyBalancePreview.builder()
                    .projectionMonth(projection.getProjectionMonth())
                    .beforeClosingBalance(projection.getClosingBalance())
                    .afterClosingBalance(afterClosingBalance)
                    .balanceDelta(afterClosingBalance - projection.getClosingBalance())
                    .build()
            );
        }

        MonthlyProjectionVO firstProjection = sortedProjections.get(0);

        int beforeMonthlyIncome = valueOf(firstProjection.getExpectedIncome());
        int beforeMonthlyExpense = valueOf(firstProjection.getExpectedExpense());
        int monthlyEffectAmount = request.recurrenceType() == SimulationRecurrenceType.MONTHLY ? effectAmount : 0;
        int onceEffectAmount = request.recurrenceType() == SimulationRecurrenceType.ONCE ? effectAmount : 0;
        int afterMonthlyIncome = beforeMonthlyIncome;
        int afterMonthlyExpense = beforeMonthlyExpense;

        if(request.recurrenceType() == SimulationRecurrenceType.MONTHLY){
            if(request.simulationItemCategory() == SimulationItemCategory.EXPENSE){
                afterMonthlyExpense = Math.max(0, beforeMonthlyExpense - effectAmount);
            }else{
                afterMonthlyIncome = beforeMonthlyIncome + effectAmount;
            }
        }

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
                    .itemName(resolveSimulationItemName(request))
                    .category(request.simulationItemCategory())
                    .monthlyEffectAmount(monthlyEffectAmount)
                    .onceEffectAmount(onceEffectAmount)
                    .build()
            )
            .build();
    }

    private void validatePreviewRequest(PreviewItemRequest request, SimulationVO simulation){
        if(request.simulationItemCategory() == SimulationItemCategory.POLICY){
            if(request.policyId() == null) throw ApplicationException.from(SimulationErrorCode.INVALID_PREVIEW_ITEM);
        }else if(request.amount() == null){
            throw ApplicationException.from(SimulationErrorCode.INVALID_PREVIEW_ITEM);
        }

        if(request.simulationItemCategory() == SimulationItemCategory.EXPENSE
            && request.simulationItemExpenseCategory() == null){
            throw ApplicationException.from(SimulationErrorCode.INVALID_PREVIEW_ITEM);
        }

        if(request.simulationItemCategory() != SimulationItemCategory.EXPENSE
            && (request.itemName() == null || request.itemName().isBlank())){
            throw ApplicationException.from(SimulationErrorCode.INVALID_PREVIEW_ITEM);
        }

        if(request.recurrenceType() == SimulationRecurrenceType.MONTHLY
            && request.simulationItemCategory() != SimulationItemCategory.EXPENSE
            && request.recurrenceDay() == null){
            throw ApplicationException.from(SimulationErrorCode.INVALID_PREVIEW_ITEM);
        }

        if(request.recurrenceDay() != null && (request.recurrenceDay() < 1 || request.recurrenceDay() > 31)){
            throw ApplicationException.from(SimulationErrorCode.INVALID_PREVIEW_ITEM);
        }

        LocalDate applyEndDate = request.applyEndDate() == null ? request.applyStartDate() : request.applyEndDate();

        if(applyEndDate.isBefore(request.applyStartDate())) throw ApplicationException.from(SimulationErrorCode.INVALID_PREVIEW_ITEM);

        if(request.applyStartDate().isAfter(simulation.getSimulationDueDate())
            || applyEndDate.isBefore(simulation.getSimulationStartDate())){
            throw ApplicationException.from(SimulationErrorCode.INVALID_PREVIEW_ITEM);
        }
    }

    private int resolveEffectAmount(PreviewItemRequest request){
        if (request.simulationItemCategory() != SimulationItemCategory.POLICY){
            return valueOf(request.amount());
        }

        PolicyVO policy = policyMapper.findById(request.policyId());

        if(policy == null) throw ApplicationException.from(CatalogErrorCode.POLICY_NOT_FOUND);

        return valueOf(policy.getPolicySupportAmount());
    }

    private String resolveSimulationItemName(PreviewItemRequest request){
        if(request.simulationItemCategory() == SimulationItemCategory.EXPENSE){
            return toKoreanExpenseCategoryName(request.simulationItemExpenseCategory()) + " 줄이기";
        }

        return request.itemName();
    }

    private String toKoreanExpenseCategoryName(ExpenseCategory expenseCategory){
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

    private int calculateMonthlyEffect(PreviewItemRequest request, LocalDate projectionMonth, int effectAmount){
        YearMonth targetMonth = YearMonth.from(projectionMonth);
        LocalDate applyStartDate = request.applyStartDate();
        LocalDate applyEndDate = request.applyEndDate() == null ? applyStartDate : request.applyEndDate();

        YearMonth applyStartMonth = YearMonth.from(applyStartDate);
        YearMonth applyEndMonth = YearMonth.from(applyEndDate);

        if(targetMonth.isBefore(applyStartMonth) || targetMonth.isAfter(applyEndMonth)) return 0;

        if(request.recurrenceType() == SimulationRecurrenceType.MONTHLY){
            if(request.simulationItemCategory() == SimulationItemCategory.EXPENSE){
                return effectAmount;
            }

            int day = Math.min(request.recurrenceDay(), targetMonth.lengthOfMonth());
            LocalDate effectDate = targetMonth.atDay(day);

            if(effectDate.isBefore(applyStartDate) || effectDate.isAfter(applyEndDate)) return 0;

            return effectAmount;
        }

        if(request.recurrenceType() == SimulationRecurrenceType.ONCE){
            return targetMonth.equals(YearMonth.from(applyStartDate)) ? effectAmount : 0;
        }

        return 0;
    }
}
