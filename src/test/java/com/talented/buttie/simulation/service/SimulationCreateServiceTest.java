package com.talented.buttie.simulation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

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
import com.talented.buttie.simulation.dto.request.CreateSimulationRequest;
import com.talented.buttie.simulation.dto.response.PreviewItemResponse;
import com.talented.buttie.simulation.exception.SimulationErrorCode;
import com.talented.buttie.simulation.mapper.MonthlyProjectionMapper;
import com.talented.buttie.simulation.mapper.SimulationItemMapper;
import com.talented.buttie.simulation.mapper.SimulationMapper;
import com.talented.buttie.snapshot.domain.FinancialSnapshotVO;
import com.talented.buttie.snapshot.exception.AnalysisErrorCode;
import com.talented.buttie.snapshot.mapper.FinancialSnapshotMapper;
import com.talented.buttie.snapshot.service.FinancialSnapshotCreateService;
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
class SimulationCreateServiceTest {

    @Mock
    private FinancialSnapshotCreateService financialSnapshotCreateService;

    @Mock
    private FinancialSnapshotMapper financialSnapshotMapper;

    @Mock
    private SimulationMapper simulationMapper;

    @Mock
    private SimulationItemMapper simulationItemMapper;

    @Mock
    private MonthlyProjectionMapper monthlyProjectionMapper;

    @Spy
    private ProjectionEngine projectionEngine = new ProjectionEngine();

    @Mock
    private EmploymentPreparationMapper employmentPreparationMapper;

    @Mock
    private PolicyMapper policyMapper;

    private SimulationItemCalculationService simulationItemCalculationService;
    private SimulationCreateService simulationCreateService;

    private Long userId;
    private CreateSimulationRequest createRequest;
    private SimulationVO activeSimulation;
    private FinancialSnapshotVO snapshot;
    private List<MonthlyProjectionVO> baseMonthlyProjections;

    @BeforeEach
    void setup() {
        simulationItemCalculationService = new SimulationItemCalculationService(
            employmentPreparationMapper,
            policyMapper,
            projectionEngine
        );
        simulationCreateService = new SimulationCreateService(
            financialSnapshotCreateService,
            financialSnapshotMapper,
            simulationMapper,
            simulationItemMapper,
            monthlyProjectionMapper,
            projectionEngine,
            employmentPreparationMapper,
            simulationItemCalculationService
        );

        userId = 1L;
        createRequest = new CreateSimulationRequest(
            LocalDate.of(2026, 8, 1),
            LocalDate.of(2027, 1, 31)
        );

        activeSimulation = SimulationVO.builder()
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
            .prepPossibleMonths(BigDecimal.valueOf(5))
            .build();

        baseMonthlyProjections = List.of(
            projection(LocalDate.of(2026, 8, 1), 5_000_000, 1_000_000, 2_000_000, 4_000_000),
            projection(LocalDate.of(2026, 9, 1), 4_000_000, 1_000_000, 2_000_000, 3_000_000),
            projection(LocalDate.of(2026, 10, 1), 3_000_000, 1_000_000, 2_000_000, 2_000_000)
        );
    }

    @Test
    @DisplayName("시뮬레이션 최초 생성 시 스냅샷을 생성하고 예상 재정 계획도 생성한다.")
    void createSimulation() {
        List<MonthlyProjectionVO> monthlyProjections = List.of(
            projection(LocalDate.of(2026, 8, 1), 5_000_000, 480_000, 525_000, 4_955_000)
        );

        FinancialSnapshotVO snapshot = FinancialSnapshotVO.builder()
            .snapshotId(10L)
            .userId(userId)
            .liquidAssets(5_000_000)
            .avgMonthlyIncome(BigDecimal.valueOf(480_000))
            .avgMonthlyExpense(BigDecimal.valueOf(525_000))
            .monthlyNetCashflow(-45_000)
            .prepPossibleMonths(BigDecimal.valueOf(8.25))
            .build();

        given(financialSnapshotCreateService.createSnapshot(userId)).willReturn(snapshot);
        given(employmentPreparationMapper.getLivingThresholdByUserId(userId)).willReturn(1_000_000);

        doAnswer(invocation -> {
            SimulationVO simulation = invocation.getArgument(0);
            simulation.setSimulationId(100L);
            return null;
        }).when(simulationMapper).save(any(SimulationVO.class));

        given(projectionEngine.createInitialProjections(
            100L,
            createRequest.simulationStartDate(),
            createRequest.simulationDueDate(),
            5_000_000,
            480_000,
            525_000,
            1_000_000
        )).willReturn(monthlyProjections);

        SimulationVO result = simulationCreateService.createSimulation(userId, createRequest);

        assertEquals(5_000_000, result.getSimulationEndAmount());
        assertEquals(monthlyProjections, result.getMonthlyProjections());
        verify(financialSnapshotCreateService).createSnapshot(userId);
        verify(simulationMapper).save(result);
        verify(monthlyProjectionMapper).saveAll(monthlyProjections);
    }

    @Test
    @DisplayName("기존 활성 시뮬레이션이 있으면 재사용한다.")
    void reuseActiveSimulation() {
        given(simulationMapper.findActiveByUserId(userId)).willReturn(activeSimulation);

        SimulationVO result = simulationCreateService.createSimulation(userId, createRequest);

        assertSame(activeSimulation, result);
        verify(simulationMapper, never()).save(any(SimulationVO.class));
        verifyNoInteractions(
            financialSnapshotCreateService,
            financialSnapshotMapper,
            employmentPreparationMapper,
            projectionEngine,
            monthlyProjectionMapper
        );
    }

    @Test
    @DisplayName("스냅샷 생성에 실패하면 예외가 전파된다.")
    void throwWhenSnapshotCreateFails() {
        given(financialSnapshotCreateService.createSnapshot(userId))
            .willThrow(ApplicationException.from(AnalysisErrorCode.SNAPSHOT_NOT_FOUND));

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationCreateService.createSimulation(userId, createRequest)
        );

        assertEquals(AnalysisErrorCode.SNAPSHOT_NOT_FOUND, exception.getCode());
        verify(simulationMapper, never()).save(any(SimulationVO.class));
        verifyNoInteractions(employmentPreparationMapper, monthlyProjectionMapper);
    }

    @Test
    @DisplayName("미리보기는 적용 확정과 같은 요청 구조로 지출 항목 효과를 계산하고 저장하지 않는다.")
    void previewMonthlyExpenseSaving() {
        stubPreviewBase();

        ApplySimulationItemRequest request = ApplySimulationItemRequest.builder()
            .category(SimulationItemCategory.EXPENSE)
            .expenseCategory(ExpenseCategory.FOOD)
            .amount(50_000)
            .applyStartDate(LocalDate.of(2026, 8, 15))
            .applyEndDate(LocalDate.of(2026, 9, 30))
            .recurrenceType(SimulationRecurrenceType.MONTHLY)
            .build();

        PreviewItemResponse result = simulationCreateService.previewItemResultSimulation(userId, request);

        assertEquals("식비 줄이기", result.itemEffect().itemName());
        assertEquals(SimulationItemCategory.EXPENSE, result.itemEffect().category());
        assertEquals(50_000, result.itemEffect().monthlyEffectAmount());
        assertEquals(0, result.itemEffect().onceEffectAmount());
        assertEquals(4_050_000, result.monthlyBalances().get(0).afterClosingBalance());
        assertEquals(3_100_000, result.monthlyBalances().get(1).afterClosingBalance());
        assertEquals(2_100_000, result.monthlyBalances().get(2).afterClosingBalance());

        verify(monthlyProjectionMapper, never()).saveAll(any());
        verifyNoInteractions(policyMapper);
    }

    @Test
    @DisplayName("정책 미리보기는 요청 반복 유형 없이 정책 반복 유형과 정책 금액을 사용한다.")
    void previewPolicyUsesPolicyAmountAndRecurrenceType() {
        stubPreviewBase();
        given(policyMapper.findById(10L))
            .willReturn(
                PolicyVO.builder()
                    .policyId(10L)
                    .policySupportAmount(300_000)
                    .policyRecurrenceType(SimulationRecurrenceType.MONTHLY)
                    .build()
            );

        ApplySimulationItemRequest request = ApplySimulationItemRequest.builder()
            .category(SimulationItemCategory.POLICY)
            .policyId(10L)
            .amount(999_999)
            .itemName("무시되는 이름")
            .applyStartDate(LocalDate.of(2026, 8, 1))
            .applyEndDate(LocalDate.of(2026, 10, 31))
            .build();

        PreviewItemResponse result = simulationCreateService.previewItemResultSimulation(userId, request);

        assertEquals(300_000, result.itemEffect().monthlyEffectAmount());
        assertEquals(0, result.itemEffect().onceEffectAmount());
        assertEquals(4_300_000, result.monthlyBalances().get(0).afterClosingBalance());
        assertEquals(3_600_000, result.monthlyBalances().get(1).afterClosingBalance());
        assertEquals(2_900_000, result.monthlyBalances().get(2).afterClosingBalance());

        verify(policyMapper).findById(10L);
        verify(monthlyProjectionMapper, never()).saveAll(any());
    }

    @Test
    @DisplayName("기존 적용 항목이 있으면 미리보기 계산에도 같이 반영한다.")
    void previewIncludesAlreadyAppliedItems() {
        given(simulationMapper.findActiveByUserId(userId)).willReturn(activeSimulation);
        given(financialSnapshotMapper.findLatestByUserId(userId)).willReturn(snapshot);
        given(monthlyProjectionMapper.findAllBySimulationId(100L)).willReturn(baseMonthlyProjections);
        given(employmentPreparationMapper.getLivingThresholdByUserId(userId)).willReturn(1_000_000);
        given(simulationItemMapper.findAllActiveBySimulationId(100L))
            .willReturn(
                List.of(
                    SimulationItemVO.builder()
                        .simulationId(100L)
                        .simulationItemCategory(SimulationItemCategory.INCOME)
                        .simulationItemName("기존 알바")
                        .simulationItemApplyAmount(100_000)
                        .applyStartDate(LocalDate.of(2026, 8, 1))
                        .applyEndDate(LocalDate.of(2026, 10, 31))
                        .recurrenceType(SimulationRecurrenceType.MONTHLY)
                        .isDeleted(false)
                        .build()
                )
            );

        ApplySimulationItemRequest request = ApplySimulationItemRequest.builder()
            .category(SimulationItemCategory.INCOME)
            .itemName("추가 알바")
            .amount(200_000)
            .applyStartDate(LocalDate.of(2026, 8, 1))
            .applyEndDate(LocalDate.of(2026, 10, 31))
            .recurrenceType(SimulationRecurrenceType.MONTHLY)
            .build();

        PreviewItemResponse result = simulationCreateService.previewItemResultSimulation(userId, request);

        assertEquals(4_300_000, result.monthlyBalances().get(0).afterClosingBalance());
        assertEquals(3_600_000, result.monthlyBalances().get(1).afterClosingBalance());
        assertEquals(2_900_000, result.monthlyBalances().get(2).afterClosingBalance());
        assertEquals(300_000, result.cashflow().netCashFlowDelta());
    }

    @Test
    @DisplayName("미리보기 적용 기간이 시뮬레이션 기간 밖이면 공통 검증 예외가 발생한다.")
    void previewThrowsWhenApplyPeriodOutOfSimulationPeriod() {
        given(simulationMapper.findActiveByUserId(userId)).willReturn(activeSimulation);

        ApplySimulationItemRequest request = ApplySimulationItemRequest.builder()
            .category(SimulationItemCategory.INCOME)
            .itemName("기간 밖 수입")
            .amount(100_000)
            .applyStartDate(LocalDate.of(2026, 11, 1))
            .recurrenceType(SimulationRecurrenceType.ONCE)
            .build();

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationCreateService.previewItemResultSimulation(userId, request)
        );

        assertEquals(SimulationErrorCode.INVALID_SIMULATION_ITEM, exception.getCode());
        verifyNoInteractions(financialSnapshotCreateService, financialSnapshotMapper, monthlyProjectionMapper, policyMapper);
    }

    @Test
    @DisplayName("미리보기 활성 시뮬레이션이 없으면 예외가 발생한다.")
    void previewThrowsWhenActiveSimulationNotFound() {
        given(simulationMapper.findActiveByUserId(userId)).willReturn(null);

        ApplySimulationItemRequest request = ApplySimulationItemRequest.builder()
            .category(SimulationItemCategory.INCOME)
            .itemName("단기 알바")
            .amount(100_000)
            .applyStartDate(LocalDate.of(2026, 8, 1))
            .recurrenceType(SimulationRecurrenceType.ONCE)
            .build();

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationCreateService.previewItemResultSimulation(userId, request)
        );

        assertEquals(SimulationErrorCode.SIMULATION_NOT_FOUND, exception.getCode());
        verifyNoInteractions(financialSnapshotCreateService, financialSnapshotMapper, monthlyProjectionMapper, policyMapper);
    }

    @Test
    @DisplayName("미리보기 정책을 찾을 수 없으면 예외가 발생한다.")
    void previewThrowsWhenPolicyNotFound() {
        given(simulationMapper.findActiveByUserId(userId)).willReturn(activeSimulation);
        given(financialSnapshotMapper.findLatestByUserId(userId)).willReturn(snapshot);
        given(monthlyProjectionMapper.findAllBySimulationId(100L)).willReturn(baseMonthlyProjections);
        given(policyMapper.findById(10L)).willReturn(null);

        ApplySimulationItemRequest request = ApplySimulationItemRequest.builder()
            .category(SimulationItemCategory.POLICY)
            .policyId(10L)
            .applyStartDate(LocalDate.of(2026, 8, 1))
            .build();

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationCreateService.previewItemResultSimulation(userId, request)
        );

        assertEquals(CatalogErrorCode.POLICY_NOT_FOUND, exception.getCode());
        verify(policyMapper).findById(10L);
        verify(monthlyProjectionMapper, never()).saveAll(any());
    }

    private void stubPreviewBase() {
        given(simulationMapper.findActiveByUserId(userId)).willReturn(activeSimulation);
        given(financialSnapshotMapper.findLatestByUserId(userId)).willReturn(snapshot);
        given(monthlyProjectionMapper.findAllBySimulationId(100L)).willReturn(baseMonthlyProjections);
        given(simulationItemMapper.findAllActiveBySimulationId(100L)).willReturn(List.of());
        given(employmentPreparationMapper.getLivingThresholdByUserId(userId)).willReturn(1_000_000);
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
