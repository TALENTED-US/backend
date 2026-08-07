package com.talented.buttie.simulation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.talented.buttie.catalog.domain.PolicyVO;
import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.simulation.domain.SimulationItemCategory;
import com.talented.buttie.simulation.domain.SimulationItemVO;
import com.talented.buttie.simulation.domain.SimulationRecurrenceType;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.dto.response.SimulationItemReportResponse;
import com.talented.buttie.simulation.dto.response.SimulationItemResponse;
import com.talented.buttie.simulation.dto.response.SimulationItemsByCategoryResponse;
import com.talented.buttie.simulation.exception.SimulationErrorCode;
import com.talented.buttie.simulation.mapper.SimulationItemMapper;
import com.talented.buttie.simulation.mapper.SimulationMapper;
import com.talented.buttie.catalog.mapper.PolicyMapper;
import com.talented.buttie.snapshot.domain.FinancialSnapshotVO;
import com.talented.buttie.snapshot.exception.AnalysisErrorCode;
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
        PKCrypto crypto = new PKCrypto("AES", "1234567890123456");
        crypto.init();

        SimulationItemCalculationService calculationService = new SimulationItemCalculationService(
            employmentPreparationMapper,
            policyMapper,
            projectionEngine
        );

        simulationItemReadService = new SimulationItemReadService(
            simulationMapper,
            simulationItemMapper,
            financialSnapshotMapper,
            calculationService,
            policyMapper
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

    // 보고서 조회 API 테스트
    @Test
    @DisplayName("적용된 항목 전체 기준으로 결과 보고서와 카테고리별 소계를 조회한다.")
    void getAppliedItemReport() {
        given(simulationMapper.findActiveByUserId(userId)).willReturn(simulation);
        given(financialSnapshotMapper.findLatestByUserId(userId)).willReturn(snapshot);
        given(employmentPreparationMapper.getLivingThresholdByUserId(userId)).willReturn(1_000_000);
        given(policyMapper.findById(7L)).willReturn(com.talented.buttie.catalog.domain.PolicyVO.builder()
            .policyId(7L)
            .supportMonthCount(6)
            .build());
        given(simulationItemMapper.findAllActiveBySimulationId(100L))
            .willReturn(List.of(
                item(SimulationItemCategory.EXPENSE, 50_000, SimulationRecurrenceType.MONTHLY),
                item(SimulationItemCategory.INCOME, 300_000, SimulationRecurrenceType.ONCE),
                item(SimulationItemCategory.POLICY, 200_000, SimulationRecurrenceType.MONTHLY)
            ));
        SimulationItemReportResponse result = simulationItemReadService.getAppliedItemReport(userId);

        assertEquals(BigDecimal.valueOf(5), result.currentPrepMonths());
        assertEquals(new BigDecimal("6.05"), result.expectPrepMonths());
        assertEquals(false, result.currentSustainable());
        assertEquals(false, result.expectSustainable());
        assertEquals(50_000, result.categoryContribution().expenseMonthlyAmount());
        assertEquals(0, result.categoryContribution().expenseOnceAmount());
        assertEquals(0, result.categoryContribution().incomeMonthlyAmount());
        assertEquals(300_000, result.categoryContribution().incomeOnceAmount());
        assertEquals(200_000, result.categoryContribution().policyMonthlyAmount());
        assertEquals(0, result.categoryContribution().policyOnceAmount());
        assertEquals(3, result.monthlyBalances().size());
        assertEquals(4_550_000, result.monthlyBalances().get(0).afterClosingBalance());
        assertEquals(1_000_000, result.monthlyBalances().get(0).livingFundThreshold());
        assertEquals(false, result.monthlyBalances().get(0).belowLivingFundThreshold());
    }

    @Test
    @DisplayName("적용 항목이 없으면 적용 전후 예측이 같고 흑자 현금흐름은 지속 가능하다.")
    void getSustainableReportWithoutAppliedItems() {
        snapshot.setLiquidAssets(0);
        snapshot.setAvgMonthlyIncome(BigDecimal.valueOf(65_666));
        snapshot.setAvgMonthlyExpense(BigDecimal.valueOf(11_633));
        snapshot.setMonthlyNetCashflow(54_033);
        snapshot.setCurrentPrepMonths(new BigDecimal("999.99"));

        given(simulationMapper.findActiveByUserId(userId)).willReturn(simulation);
        given(financialSnapshotMapper.findLatestByUserId(userId)).willReturn(snapshot);
        given(employmentPreparationMapper.getLivingThresholdByUserId(userId)).willReturn(1_000_000);
        given(simulationItemMapper.findAllActiveBySimulationId(100L)).willReturn(List.of());

        SimulationItemReportResponse result = simulationItemReadService.getAppliedItemReport(userId);

        assertEquals(new BigDecimal("999.99"), result.currentPrepMonths());
        assertEquals(new BigDecimal("999.99"), result.expectPrepMonths());
        assertEquals(true, result.currentSustainable());
        assertEquals(true, result.expectSustainable());
        assertEquals(0, result.cashflow().incomeDelta());
        assertEquals(0, result.cashflow().expenseDelta());
        assertEquals(0, result.cashflow().netCashFlowDelta());
        assertEquals(true, result.monthlyBalances().get(0).belowLivingFundThreshold());
        result.monthlyBalances().forEach(month ->
            assertEquals(month.beforeClosingBalance(), month.afterClosingBalance())
        );
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
            .policyId(category == SimulationItemCategory.POLICY ? 7L : null)
            .recurrenceType(recurrenceType)
            .isDeleted(false)
            .build();
    }

    // 카테고리별 적용 항목 리스트 조회 API
    @Test
    @DisplayName("정책 카테고리 항목 목록을 조회한다.")
    void findPolicyItemsByCategory() {
        SimulationItemVO item = SimulationItemVO.builder()
            .simulationItemId(1L)
            .simulationId(100L)
            .simulationItemCategory(SimulationItemCategory.POLICY)
            .simulationItemApplyAmount(200_000)
            .applyStartDate(LocalDate.of(2026, 8, 1))
            .policyId(7L)
            .recurrenceType(SimulationRecurrenceType.MONTHLY)
            .isDeleted(false)
            .build();

        PolicyVO policy = PolicyVO.builder()
            .policyId(7L)
            .policyName("청년월세 특별지원")
            .policySupportAmount(200_000)
            .supportMonthCount(12)
            .build();

        given(simulationMapper.findActiveByUserId(userId))
            .willReturn(simulation);
        given(simulationItemMapper.findAllByCategory(simulation.getSimulationId(), SimulationItemCategory.POLICY))
            .willReturn(List.of(item));
        given(policyMapper.findById(7L)).willReturn(policy);

        SimulationItemsByCategoryResponse result = simulationItemReadService.findItemListByCategory(
            userId, SimulationItemCategory.POLICY
        );

        assertEquals(1, result.appliedItems().size());

        SimulationItemResponse response = result.appliedItems().get(0);

        assertEquals(SimulationItemCategory.POLICY, response.itemCategory());
        assertEquals("청년월세 특별지원", response.displayName());
        assertEquals(200_000, response.amount());
        assertEquals(SimulationRecurrenceType.MONTHLY, response.recurrenceType());
        assertEquals(12, response.policy().supportMonthCount());

        verify(simulationItemMapper).findAllByCategory(simulation.getSimulationId(), SimulationItemCategory.POLICY);
    }

    @Test
    @DisplayName("카테고리에 적용된 항목이 없으면 빈 목록을 반환한다.")
    void findEmptyItemsByCategory() {
        given(simulationMapper.findActiveByUserId(userId))
            .willReturn(simulation);
        given(simulationItemMapper.findAllByCategory(simulation.getSimulationId(), SimulationItemCategory.EXPENSE))
            .willReturn(List.of());

        SimulationItemsByCategoryResponse result = simulationItemReadService.findItemListByCategory(
            userId, SimulationItemCategory.EXPENSE
        );

        assertEquals(0, result.appliedItems().size());
        verifyNoInteractions(policyMapper);
    }

    // 시뮬레이션 통합 조회용 부가 데이터 조회
    @Test
    @DisplayName("시뮬레이션에 적용된 전체 항목 목록을 카테고리 구분 없이 조회한다.")
    void findAllAppliedItems() {
        SimulationItemVO expenseItem = item(SimulationItemCategory.EXPENSE, 50_000, SimulationRecurrenceType.MONTHLY);
        expenseItem.setSimulationItemId(1L);
        SimulationItemVO policyItem = item(SimulationItemCategory.POLICY, 200_000, SimulationRecurrenceType.MONTHLY);
        policyItem.setSimulationItemId(2L);

        given(simulationItemMapper.findAllActiveBySimulationId(100L))
            .willReturn(List.of(expenseItem, policyItem));
        given(policyMapper.findById(7L)).willReturn(PolicyVO.builder()
            .policyId(7L)
            .policyName("청년월세 특별지원")
            .supportMonthCount(12)
            .build());

        List<SimulationItemResponse> result = simulationItemReadService.findAllAppliedItems(simulation);

        assertEquals(2, result.size());
        verify(simulationItemMapper).findAllActiveBySimulationId(100L);
    }

    @Test
    @DisplayName("사용자의 최신 재정 스냅샷에서 현재 버티는 기간을 조회한다.")
    void getCurrentPrepMonths() {
        given(financialSnapshotMapper.findLatestByUserId(userId)).willReturn(snapshot);

        BigDecimal result = simulationItemReadService.getCurrentPrepMonths(userId);

        assertEquals(BigDecimal.valueOf(5), result);
    }

    @Test
    @DisplayName("재정 스냅샷이 없으면 예외가 발생한다.")
    void throwWhenSnapshotNotFoundForCurrentPrepMonths() {
        given(financialSnapshotMapper.findLatestByUserId(userId)).willReturn(null);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationItemReadService.getCurrentPrepMonths(userId)
        );

        assertEquals(AnalysisErrorCode.SNAPSHOT_NOT_FOUND, exception.getCode());
    }
}
