package com.talented.buttie.simulation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.simulation.domain.MonthlyProjectionVO;
import com.talented.buttie.simulation.domain.SimulationItemCategory;
import com.talented.buttie.simulation.domain.SimulationItemVO;
import com.talented.buttie.simulation.domain.SimulationRecurrenceType;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.dto.response.SimulationItemReportResponse;
import com.talented.buttie.simulation.exception.SimulationErrorCode;
import com.talented.buttie.simulation.mapper.MonthlyProjectionMapper;
import com.talented.buttie.simulation.mapper.SimulationItemMapper;
import com.talented.buttie.simulation.mapper.SimulationMapper;
import com.talented.buttie.catalog.mapper.PolicyMapper;
import com.talented.buttie.snapshot.domain.FinancialSnapshotVO;
import com.talented.buttie.snapshot.mapper.FinancialSnapshotMapper;
import com.talented.buttie.user.mapper.EmploymentPreparationMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SimulationItemReadServiceTest {

    @Mock
    private SimulationMapper simulationMapper;

    @Mock
    private SimulationItemMapper simulationItemMapper;

    @Mock
    private MonthlyProjectionMapper monthlyProjectionMapper;

    @Mock
    private FinancialSnapshotMapper financialSnapshotMapper;

    @Mock
    private EmploymentPreparationMapper employmentPreparationMapper;

    @Mock
    private PolicyMapper policyMapper;

    @Spy
    private ProjectionEngine projectionEngine = new ProjectionEngine();

    private SimulationItemReadService simulationItemReadService;

    private Long userId;
    private SimulationVO simulation;
    private FinancialSnapshotVO snapshot;

    @BeforeEach
    void setUp() {
        SimulationItemCalculationService calculationService = new SimulationItemCalculationService(
            employmentPreparationMapper,
            policyMapper,
            projectionEngine
        );

        simulationItemReadService = new SimulationItemReadService(
            simulationMapper,
            simulationItemMapper,
            monthlyProjectionMapper,
            financialSnapshotMapper,
            calculationService
        );

        userId = 1L;
        simulation = SimulationVO.builder()
            .simulationId(100L)
            .userId(userId)
            .snapshotId(10L)
            .simulationStartDate(LocalDate.of(2026, 8, 1))
            .simulationDueDate(LocalDate.of(2026, 10, 31))
            .build();

        snapshot = FinancialSnapshotVO.builder()
            .snapshotId(10L)
            .userId(userId)
            .liquidAssets(5_000_000)
            .avgMonthlyIncome(BigDecimal.valueOf(1_000_000))
            .avgMonthlyExpense(BigDecimal.valueOf(2_000_000))
            .monthlyNetCashflow(-1_000_000)
            .currentPrepMonths(BigDecimal.valueOf(5))
            .build();
    }

    @Test
    @DisplayName("적용된 항목 전체 기준으로 결과 보고서와 카테고리별 소계를 조회한다.")
    void getAppliedItemReport() {
        given(simulationMapper.findActiveByUserId(userId)).willReturn(simulation);
        given(financialSnapshotMapper.findLatestByUserId(userId)).willReturn(snapshot);
        given(employmentPreparationMapper.getLivingThresholdByUserId(userId)).willReturn(1_000_000);
        given(simulationItemMapper.findAllActiveBySimulationId(100L))
            .willReturn(List.of(
                item(SimulationItemCategory.EXPENSE, 50_000, SimulationRecurrenceType.MONTHLY),
                item(SimulationItemCategory.INCOME, 300_000, SimulationRecurrenceType.ONCE),
                item(SimulationItemCategory.POLICY, 200_000, SimulationRecurrenceType.MONTHLY)
            ));
        given(monthlyProjectionMapper.findAllBySimulationId(100L))
            .willReturn(List.of(
                projection(LocalDate.of(2026, 8, 1), 5_000_000, 1_200_000, 1_950_000, 4_250_000),
                projection(LocalDate.of(2026, 9, 1), 4_250_000, 1_500_000, 1_950_000, 3_800_000),
                projection(LocalDate.of(2026, 10, 1), 3_800_000, 1_200_000, 1_950_000, 3_050_000)
            ));

        SimulationItemReportResponse result = simulationItemReadService.getAppliedItemReport(userId);

        assertEquals(BigDecimal.valueOf(5), result.currentPrepMonths());
        assertEquals(new BigDecimal("6.05"), result.expectPrepMonths());
        assertEquals(50_000, result.categoryContribution().expenseMonthlyAmount());
        assertEquals(0, result.categoryContribution().expenseOnceAmount());
        assertEquals(0, result.categoryContribution().incomeMonthlyAmount());
        assertEquals(300_000, result.categoryContribution().incomeOnceAmount());
        assertEquals(200_000, result.categoryContribution().policyMonthlyAmount());
        assertEquals(0, result.categoryContribution().policyOnceAmount());
        assertEquals(3, result.monthlyBalances().size());
        assertEquals(4_250_000, result.monthlyBalances().get(0).afterClosingBalance());
        assertEquals(1_000_000, result.monthlyBalances().get(0).livingFundThreshold());

        verify(monthlyProjectionMapper, never()).saveAll(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("활성 시뮬레이션이 없으면 결과 보고서 조회 예외가 발생한다.")
    void throwWhenActiveSimulationNotFound() {
        given(simulationMapper.findActiveByUserId(userId)).willReturn(null);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationItemReadService.getAppliedItemReport(userId)
        );

        assertEquals(SimulationErrorCode.SIMULATION_NOT_FOUND, exception.getCode());
    }

    private SimulationItemVO item(
        SimulationItemCategory category,
        int amount,
        SimulationRecurrenceType recurrenceType
    ) {
        return SimulationItemVO.builder()
            .simulationId(100L)
            .simulationItemCategory(category)
            .simulationItemName(category == SimulationItemCategory.EXPENSE ? "식비 줄이기" : null)
            .simulationItemExpenseCategory(category == SimulationItemCategory.EXPENSE ? ExpenseCategory.FOOD : null)
            .simulationItemApplyAmount(amount)
            .applyStartDate(LocalDate.of(2026, 8, 1))
            .applyEndDate(LocalDate.of(2026, 10, 31))
            .recurrenceType(recurrenceType)
            .isDeleted(false)
            .build();
    }

    private MonthlyProjectionVO projection(
        LocalDate projectionMonth,
        int openingBalance,
        int expectedIncome,
        int expectedExpense,
        int closingBalance
    ) {
        return MonthlyProjectionVO.builder()
            .simulationId(100L)
            .projectionMonth(projectionMonth)
            .openingBalance(openingBalance)
            .expectedIncome(expectedIncome)
            .expectedExpense(expectedExpense)
            .closingBalance(closingBalance)
            .adjustmentRequired(false)
            .build();
    }
}
