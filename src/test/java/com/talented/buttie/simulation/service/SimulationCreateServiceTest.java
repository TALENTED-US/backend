package com.talented.buttie.simulation.service;

import static org.junit.jupiter.api.Assertions.*;
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
import com.talented.buttie.simulation.domain.SimulationRecurrenceType;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.dto.request.CreateSimulationRequest;
import com.talented.buttie.simulation.dto.request.PreviewItemRequest;
import com.talented.buttie.simulation.dto.response.PreviewItemResponse;
import com.talented.buttie.simulation.exception.SimulationErrorCode;
import com.talented.buttie.simulation.mapper.MonthlyProjectionMapper;
import com.talented.buttie.simulation.mapper.SimulationMapper;
import com.talented.buttie.snapshot.domain.FinancialSnapshotVO;
import com.talented.buttie.snapshot.exception.AnalysisErrorCode;
import com.talented.buttie.snapshot.service.FinancialSnapshotCreateService;
import com.talented.buttie.user.mapper.EmploymentPreparationMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class SimulationCreateServiceTest {

    @Mock
    private FinancialSnapshotCreateService financialSnapshotCreateService;

    @Mock
    private SimulationMapper simulationMapper;

    @Mock
    private MonthlyProjectionMapper monthlyProjectionMapper;

    @Mock
    private ProjectionEngine projectionEngine;

    @Mock
    private EmploymentPreparationMapper employmentPreparationMapper;

    @Mock
    private PolicyMapper policyMapper;

    @InjectMocks
    private SimulationCreateService simulationCreateService;

    private Long userId;
    private CreateSimulationRequest request;
    private SimulationVO activeSimulation;
    private List<MonthlyProjectionVO> baseMonthlyProjections;

    @BeforeEach
    void setup(){
        userId = 1L;
        request = new CreateSimulationRequest(
            LocalDate.of(2026, 8, 1),
            LocalDate.of(2027, 1, 31)
        );

        activeSimulation = SimulationVO.builder()
            .simulationId(100L)
            .userId(userId)
            .simulationStartDate(LocalDate.of(2026, 8, 1))
            .simulationDueDate(LocalDate.of(2026, 10, 31))
            .build();

        baseMonthlyProjections = List.of(
            MonthlyProjectionVO.builder()
                .simulationId(100L)
                .projectionMonth(LocalDate.of(2026, 8, 1))
                .openingBalance(5_000_000)
                .expectedIncome(1_000_000)
                .expectedExpense(2_000_000)
                .closingBalance(4_000_000)
                .adjustmentRequired(false)
                .build(),
            MonthlyProjectionVO.builder()
                .simulationId(100L)
                .projectionMonth(LocalDate.of(2026, 9, 1))
                .openingBalance(4_000_000)
                .expectedIncome(1_000_000)
                .expectedExpense(2_000_000)
                .closingBalance(3_000_000)
                .adjustmentRequired(false)
                .build(),
            MonthlyProjectionVO.builder()
                .simulationId(100L)
                .projectionMonth(LocalDate.of(2026, 10, 1))
                .openingBalance(3_000_000)
                .expectedIncome(1_000_000)
                .expectedExpense(2_000_000)
                .closingBalance(2_000_000)
                .adjustmentRequired(false)
                .build()
        );
    }

    @Test
    @DisplayName("시뮬레이션 최초 생성 테스트(예상 재정 계획도 생성)")
    void createSimulation() {
        FinancialSnapshotVO snapshot = FinancialSnapshotVO.builder()
            .snapshotId(10L)
            .userId(userId)
            .liquidAssets(5_000_000)
            .avgMonthlyIncome(new BigDecimal("480000"))
            .avgMonthlyExpense(new BigDecimal("525000"))
            .monthlyNetCashflow(500_000)
            .prepPossibleMonths(new BigDecimal("8.25"))
            .build();

        List<MonthlyProjectionVO> monthlyProjections = List.of(
            MonthlyProjectionVO.builder()
                .simulationId(100L)
                .projectionMonth(LocalDate.of(2026, 8, 1))
                .openingBalance(5_000_000)
                .expectedIncome(480_000)
                .expectedExpense(525_000)
                .closingBalance(4_955_000)
                .adjustmentRequired(false)
                .build()
        );

        given(financialSnapshotCreateService.createSnapshot(userId))
            .willReturn(snapshot);
        given(employmentPreparationMapper.getLivingThresholdByUserId(userId))
            .willReturn(1_000_000);

        doAnswer(invocation -> {
            SimulationVO simulation = invocation.getArgument(0);
            simulation.setSimulationId(100L);
            return null;
        }).when(simulationMapper).save(any(SimulationVO.class));

        given(projectionEngine.createInitialProjections(
            100L, request.simulationStartDate(), request.simulationDueDate(),
            5_000_000, 480_000, 525_000, 1_000_000
        )).willReturn(monthlyProjections);

        // when: 실제 테스트 실행
        SimulationVO result = simulationCreateService.createSimulation(userId, request);

        // then: 결과 확인
        assertEquals(5_000_000, result.getSimulationEndAmount());
        assertEquals(monthlyProjections, result.getMonthlyProjections());

        verify(simulationMapper).save(result);
        verify(monthlyProjectionMapper).saveAll(monthlyProjections);
    }

    @Test
    @DisplayName("기존 활성 시뮬레이션이 있으면 재사용한다.")
    void reuseActiveSimulation() {
        SimulationVO activeSimulation = SimulationVO.builder()
            .simulationId(100L)
            .userId(userId)
            .build();

        given(simulationMapper.findActiveByUserId(userId))
            .willReturn(activeSimulation);

        SimulationVO result = simulationCreateService.createSimulation(userId, request);

        assertSame(activeSimulation, result);
        verify(simulationMapper, never()).save(any(SimulationVO.class));
        verifyNoInteractions(financialSnapshotCreateService, employmentPreparationMapper, projectionEngine, monthlyProjectionMapper);
    }

    @Test
    @DisplayName("스냅샷 생성에 실패하면 예외가 전파된다.")
    void throwWhenSnapshotCreateFails() {
        given(financialSnapshotCreateService.createSnapshot(userId))
            .willThrow(ApplicationException.from(AnalysisErrorCode.SNAPSHOT_NOT_FOUND));

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationCreateService.createSimulation(userId, request)
        );

        assertEquals(AnalysisErrorCode.SNAPSHOT_NOT_FOUND, exception.getCode());
        verify(simulationMapper, never()).save(any(SimulationVO.class));
        verifyNoInteractions(employmentPreparationMapper, projectionEngine, monthlyProjectionMapper);
    }

    @Test
    @DisplayName("미리보기 - 매달 지출 절약 항목은 월 지출을 줄이고 이후 잔액에 누적 반영된다.")
    void previewMonthlyExpenseSaving() {
        given(simulationMapper.findActiveByUserId(userId))
            .willReturn(activeSimulation);
        given(monthlyProjectionMapper.findAllBySimulationId(100L))
            .willReturn(baseMonthlyProjections);

        PreviewItemRequest request = PreviewItemRequest.builder()
            .simulationItemCategory(SimulationItemCategory.EXPENSE)
            .itemName("식비 절약")
            .simulationItemExpenseCategory(ExpenseCategory.FOOD)
            .amount(50_000)
            .applyStartDate(LocalDate.of(2026, 8, 15))
            .applyEndDate(LocalDate.of(2026, 9, 30))
            .recurrenceType(SimulationRecurrenceType.MONTHLY)
            .build();

        PreviewItemResponse result = simulationCreateService.previewItemResultSimulation(userId, request);

        assertEquals(3, result.monthlyBalances().size());
        assertEquals(4_050_000, result.monthlyBalances().get(0).afterClosingBalance());
        assertEquals(50_000, result.monthlyBalances().get(0).balanceDelta());

        assertEquals(3_100_000, result.monthlyBalances().get(1).afterClosingBalance());
        assertEquals(100_000, result.monthlyBalances().get(1).balanceDelta());

        assertEquals(2_100_000, result.monthlyBalances().get(2).afterClosingBalance());
        assertEquals(100_000, result.monthlyBalances().get(2).balanceDelta());

        assertEquals(1_000_000, result.cashflow().beforeMonthlyIncome());
        assertEquals(1_000_000, result.cashflow().afterMonthlyIncome());
        assertEquals(2_000_000, result.cashflow().beforeMonthlyExpense());
        assertEquals(1_950_000, result.cashflow().afterMonthlyExpense());
        assertEquals(50_000, result.cashflow().netCashFlowDelta());

        assertEquals("식비 줄이기", result.itemEffect().itemName());
        assertEquals(SimulationItemCategory.EXPENSE, result.itemEffect().category());
        assertEquals(50_000, result.itemEffect().monthlyEffectAmount());
        assertEquals(0, result.itemEffect().onceEffectAmount());

        verify(monthlyProjectionMapper, never()).saveAll(any());
        verifyNoInteractions(policyMapper);
    }

    @Test
    @DisplayName("미리보기 - 매달 수입 항목은 월 수입을 늘리고 잔액에 누적 반영된다.")
    void previewMonthlyIncome() {
        given(simulationMapper.findActiveByUserId(userId))
            .willReturn(activeSimulation);
        given(monthlyProjectionMapper.findAllBySimulationId(100L))
            .willReturn(baseMonthlyProjections);

        PreviewItemRequest request = PreviewItemRequest.builder()
            .simulationItemCategory(SimulationItemCategory.INCOME)
            .itemName("정기 알바")
            .amount(300_000)
            .applyStartDate(LocalDate.of(2026, 8, 1))
            .applyEndDate(LocalDate.of(2026, 10, 31))
            .recurrenceType(SimulationRecurrenceType.MONTHLY)
            .recurrenceDay(10)
            .build();

        PreviewItemResponse result = simulationCreateService.previewItemResultSimulation(userId, request);

        assertEquals(4_300_000, result.monthlyBalances().get(0).afterClosingBalance());
        assertEquals(300_000, result.monthlyBalances().get(0).balanceDelta());

        assertEquals(3_600_000, result.monthlyBalances().get(1).afterClosingBalance());
        assertEquals(600_000, result.monthlyBalances().get(1).balanceDelta());

        assertEquals(2_900_000, result.monthlyBalances().get(2).afterClosingBalance());
        assertEquals(900_000, result.monthlyBalances().get(2).balanceDelta());

        assertEquals(1_000_000, result.cashflow().beforeMonthlyIncome());
        assertEquals(1_300_000, result.cashflow().afterMonthlyIncome());
        assertEquals(300_000, result.cashflow().incomeDelta());
        assertEquals(300_000, result.cashflow().netCashFlowDelta());

        assertEquals("정기 알바", result.itemEffect().itemName());
        assertEquals(SimulationItemCategory.INCOME, result.itemEffect().category());
        assertEquals(300_000, result.itemEffect().monthlyEffectAmount());
        assertEquals(0, result.itemEffect().onceEffectAmount());

        verify(monthlyProjectionMapper, never()).saveAll(any());
        verifyNoInteractions(policyMapper);
    }

    @Test
    @DisplayName("미리보기 - 일회성 수입 항목은 적용 월에만 반영되고 이후 잔액에 누적된다.")
    void previewOnceIncome() {
        given(simulationMapper.findActiveByUserId(userId))
            .willReturn(activeSimulation);
        given(monthlyProjectionMapper.findAllBySimulationId(100L))
            .willReturn(baseMonthlyProjections);

        PreviewItemRequest request = PreviewItemRequest.builder()
            .simulationItemCategory(SimulationItemCategory.INCOME)
            .itemName("단기 알바")
            .amount(500_000)
            .applyStartDate(LocalDate.of(2026, 9, 10))
            .recurrenceType(SimulationRecurrenceType.ONCE)
            .build();

        PreviewItemResponse result = simulationCreateService.previewItemResultSimulation(userId, request);

        assertEquals(4_000_000, result.monthlyBalances().get(0).afterClosingBalance());
        assertEquals(0, result.monthlyBalances().get(0).balanceDelta());

        assertEquals(3_500_000, result.monthlyBalances().get(1).afterClosingBalance());
        assertEquals(500_000, result.monthlyBalances().get(1).balanceDelta());

        assertEquals(2_500_000, result.monthlyBalances().get(2).afterClosingBalance());
        assertEquals(500_000, result.monthlyBalances().get(2).balanceDelta());

        assertEquals(0, result.itemEffect().monthlyEffectAmount());
        assertEquals(500_000, result.itemEffect().onceEffectAmount());

        verify(monthlyProjectionMapper, never()).saveAll(any());
        verifyNoInteractions(policyMapper);
    }

    @Test
    @DisplayName("미리보기 - 매달 정책 항목은 요청 amount가 아니라 정책 지원 금액을 사용한다.")
    void previewMonthlyPolicy() {
        given(simulationMapper.findActiveByUserId(userId))
            .willReturn(activeSimulation);
        given(monthlyProjectionMapper.findAllBySimulationId(100L))
            .willReturn(baseMonthlyProjections);
        given(policyMapper.findById(10L))
            .willReturn(
                PolicyVO.builder()
                    .policyId(10L)
                    .policySupportAmount(300_000)
                    .build()
            );

        PreviewItemRequest request = PreviewItemRequest.builder()
            .simulationItemCategory(SimulationItemCategory.POLICY)
            .itemName("청년 지원금")
            .amount(999_999)
            .policyId(10L)
            .applyStartDate(LocalDate.of(2026, 8, 1))
            .applyEndDate(LocalDate.of(2026, 10, 31))
            .recurrenceType(SimulationRecurrenceType.MONTHLY)
            .recurrenceDay(10)
            .build();

        PreviewItemResponse result = simulationCreateService.previewItemResultSimulation(userId, request);

        assertEquals(4_300_000, result.monthlyBalances().get(0).afterClosingBalance());
        assertEquals(3_600_000, result.monthlyBalances().get(1).afterClosingBalance());
        assertEquals(2_900_000, result.monthlyBalances().get(2).afterClosingBalance());

        assertEquals(300_000, result.itemEffect().monthlyEffectAmount());
        assertEquals(0, result.itemEffect().onceEffectAmount());

        verify(policyMapper).findById(10L);
        verify(monthlyProjectionMapper, never()).saveAll(any());
    }

    @Test
    @DisplayName("미리보기 - 일회성 정책 항목은 적용 월에만 정책 지원 금액을 반영한다.")
    void previewOncePolicy() {
        given(simulationMapper.findActiveByUserId(userId))
            .willReturn(activeSimulation);
        given(monthlyProjectionMapper.findAllBySimulationId(100L))
            .willReturn(baseMonthlyProjections);
        given(policyMapper.findById(10L))
            .willReturn(
                PolicyVO.builder()
                    .policyId(10L)
                    .policySupportAmount(700_000)
                    .build()
            );

        PreviewItemRequest request = PreviewItemRequest.builder()
            .simulationItemCategory(SimulationItemCategory.POLICY)
            .itemName("취업 지원금")
            .policyId(10L)
            .applyStartDate(LocalDate.of(2026, 10, 5))
            .recurrenceType(SimulationRecurrenceType.ONCE)
            .build();

        PreviewItemResponse result = simulationCreateService.previewItemResultSimulation(userId, request);

        assertEquals(4_000_000, result.monthlyBalances().get(0).afterClosingBalance());
        assertEquals(3_000_000, result.monthlyBalances().get(1).afterClosingBalance());
        assertEquals(2_700_000, result.monthlyBalances().get(2).afterClosingBalance());

        assertEquals(0, result.itemEffect().monthlyEffectAmount());
        assertEquals(700_000, result.itemEffect().onceEffectAmount());

        verify(policyMapper).findById(10L);
        verify(monthlyProjectionMapper, never()).saveAll(any());
    }

    @Test
    @DisplayName("미리보기 - 적용 기간이 시뮬레이션 기간 밖이면 예외가 발생한다.")
    void previewThrowsWhenApplyPeriodOutOfSimulationPeriod() {
        given(simulationMapper.findActiveByUserId(userId))
            .willReturn(activeSimulation);
        given(monthlyProjectionMapper.findAllBySimulationId(100L))
            .willReturn(baseMonthlyProjections);

        PreviewItemRequest request = PreviewItemRequest.builder()
            .simulationItemCategory(SimulationItemCategory.INCOME)
            .itemName("단기 알바")
            .amount(100_000)
            .applyStartDate(LocalDate.of(2026, 11, 1))
            .recurrenceType(SimulationRecurrenceType.ONCE)
            .build();

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationCreateService.previewItemResultSimulation(userId, request)
        );

        assertEquals(SimulationErrorCode.INVALID_PREVIEW_ITEM, exception.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode().getHttpStatus());
        verifyNoInteractions(policyMapper);
    }

    @Test
    @DisplayName("미리보기 - 값 문제가 있으면 예외가 발생한다.")
    void previewThrowsWhenRequestValueInvalid() {
        given(simulationMapper.findActiveByUserId(userId))
            .willReturn(activeSimulation);
        given(monthlyProjectionMapper.findAllBySimulationId(100L))
            .willReturn(baseMonthlyProjections);

        PreviewItemRequest request = PreviewItemRequest.builder()
            .simulationItemCategory(SimulationItemCategory.INCOME)
            .itemName("정기 알바")
            .amount(100_000)
            .applyStartDate(LocalDate.of(2026, 8, 1))
            .recurrenceType(SimulationRecurrenceType.MONTHLY)
            .build();

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationCreateService.previewItemResultSimulation(userId, request)
        );

        assertEquals(SimulationErrorCode.INVALID_PREVIEW_ITEM, exception.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode().getHttpStatus());
        verifyNoInteractions(policyMapper);
    }

    @Test
    @DisplayName("미리보기 - 활성 시뮬레이션이 없으면 예외가 발생한다.")
    void previewThrowsWhenActiveSimulationNotFound() {
        given(simulationMapper.findActiveByUserId(userId))
            .willReturn(null);

        PreviewItemRequest request = PreviewItemRequest.builder()
            .simulationItemCategory(SimulationItemCategory.INCOME)
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
        assertEquals(HttpStatus.NOT_FOUND, exception.getCode().getHttpStatus());
        verifyNoInteractions(monthlyProjectionMapper, policyMapper);
    }

    @Test
    @DisplayName("미리보기 - 예상 재정 계획이 없으면 예외가 발생한다.")
    void previewThrowsWhenProjectionNotFound() {
        given(simulationMapper.findActiveByUserId(userId))
            .willReturn(activeSimulation);
        given(monthlyProjectionMapper.findAllBySimulationId(100L))
            .willReturn(List.of());

        PreviewItemRequest request = PreviewItemRequest.builder()
            .simulationItemCategory(SimulationItemCategory.INCOME)
            .itemName("단기 알바")
            .amount(100_000)
            .applyStartDate(LocalDate.of(2026, 8, 1))
            .recurrenceType(SimulationRecurrenceType.ONCE)
            .build();

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationCreateService.previewItemResultSimulation(userId, request)
        );

        assertEquals(SimulationErrorCode.SIMULATION_PROJECTION_NOT_FOUND, exception.getCode());
        assertEquals(HttpStatus.NOT_FOUND, exception.getCode().getHttpStatus());
        verifyNoInteractions(policyMapper);
    }


    @Test
    @DisplayName("미리보기 - 정책을 찾을 수 없으면 예외가 발생한다.")
    void previewThrowsWhenPolicyNotFound() {
        given(simulationMapper.findActiveByUserId(userId))
            .willReturn(activeSimulation);
        given(monthlyProjectionMapper.findAllBySimulationId(100L))
            .willReturn(baseMonthlyProjections);
        given(policyMapper.findById(10L))
            .willReturn(null);

        PreviewItemRequest request = PreviewItemRequest.builder()
            .simulationItemCategory(SimulationItemCategory.POLICY)
            .itemName("청년 지원금")
            .policyId(10L)
            .applyStartDate(LocalDate.of(2026, 8, 1))
            .recurrenceType(SimulationRecurrenceType.ONCE)
            .build();

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationCreateService.previewItemResultSimulation(userId, request)
        );

        assertEquals(CatalogErrorCode.POLICY_NOT_FOUND, exception.getCode());
        verify(policyMapper).findById(10L);
        verify(monthlyProjectionMapper, never()).saveAll(any());
    }


}
