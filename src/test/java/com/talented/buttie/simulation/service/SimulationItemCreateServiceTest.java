package com.talented.buttie.simulation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.simulation.domain.MonthlyProjectionVO;
import com.talented.buttie.simulation.domain.SimulationItemCategory;
import com.talented.buttie.simulation.domain.SimulationItemVO;
import com.talented.buttie.simulation.domain.SimulationRecurrenceType;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.dto.request.ApplySimulationItemRequest;
import com.talented.buttie.simulation.dto.response.ApplySimulationItemResponse;
import com.talented.buttie.simulation.exception.SimulationErrorCode;
import com.talented.buttie.simulation.mapper.MonthlyProjectionMapper;
import com.talented.buttie.simulation.mapper.SimulationItemMapper;
import com.talented.buttie.simulation.mapper.SimulationMapper;
import com.talented.buttie.snapshot.domain.FinancialSnapshotVO;
import com.talented.buttie.snapshot.exception.AnalysisErrorCode;
import com.talented.buttie.snapshot.mapper.FinancialSnapshotMapper;
import com.talented.buttie.user.mapper.EmploymentPreparationMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SimulationItemCreateServiceTest {

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

    private SimulationItemCalculationService simulationItemCalculationService;
    private SimulationItemCreateService simulationItemCreateService;

    private Long userId;
    private SimulationVO simulation;
    private FinancialSnapshotVO snapshot;
    private List<MonthlyProjectionVO> beforeProjections;

    @BeforeEach
    void setUp() {
        PKCrypto crypto = new PKCrypto("AES", "1234567890123456");
        crypto.init();

        simulationItemCalculationService = new SimulationItemCalculationService(
            employmentPreparationMapper,
            policyMapper,
            projectionEngine
        );
        simulationItemCreateService = new SimulationItemCreateService(
            simulationMapper,
            simulationItemMapper,
            monthlyProjectionMapper,
            financialSnapshotMapper,
            simulationItemCalculationService
        );

        userId = 1L;

        simulation = SimulationVO.builder()
            .simulationId(100L)
            .userId(userId)
            .snapshotId(10L)
            .simulationStartDate(LocalDate.of(2026, 8, 1))
            .simulationDueDate(LocalDate.of(2026, 10, 31))
            .simulationEndAmount(4_000_000)
            .expectPrepMonths(BigDecimal.valueOf(3))
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

        beforeProjections = List.of(
            projection(LocalDate.of(2026, 8, 1), 5_000_000, 1_000_000, 2_000_000, 4_000_000),
            projection(LocalDate.of(2026, 9, 1), 4_000_000, 1_000_000, 2_000_000, 3_000_000),
            projection(LocalDate.of(2026, 10, 1), 3_000_000, 1_000_000, 2_000_000, 2_000_000)
        );
    }

    @Test
    @DisplayName("초기 자산이 0원이어도 해당 월 수입으로 회복되면 이후 소진 시점까지의 기간을 반영한다.")
    void calculatePrepMonthsWhenZeroAssetsRecoverWithinMonth() {
        List<MonthlyProjectionVO> projections = List.of(
            projection(LocalDate.of(2026, 8, 1), 0, 65_666, 11_633, 54_033)
        );

        BigDecimal result = simulationItemCalculationService.calculateExpectedPrepMonths(
            projections,
            snapshot
        );

        assertEquals(new BigDecimal("1.05"), result);
    }

    @Test
    @DisplayName("초기 자산이 0원이고 회복 없이 계속 순소진되면 버티는 기간은 0개월이다.")
    void calculateZeroPrepMonthsWithoutAssets() {
        List<MonthlyProjectionVO> projections = List.of(
            projection(LocalDate.of(2026, 8, 1), 0, 11_633, 65_666, -54_033)
        );

        BigDecimal result = simulationItemCalculationService.calculateExpectedPrepMonths(
            projections,
            snapshot
        );

        assertEquals(new BigDecimal("0.00"), result);
    }

    @Test
    @DisplayName("중간 달에 잔액이 마이너스가 되어도 다음 달 수입으로 회복되면 시뮬레이션 종료 이후 소진 시점까지 반영한다.")
    void calculatePrepMonthsWhenNegativeMonthRecoversLater() {
        List<MonthlyProjectionVO> projections = List.of(
            projection(LocalDate.of(2026, 8, 1), 0, 0, 50_566, -50_566),
            projection(LocalDate.of(2026, 9, 1), -50_566, 600_000, 50_566, 498_868),
            projection(LocalDate.of(2026, 10, 1), 498_868, 0, 50_566, 448_302),
            projection(LocalDate.of(2026, 11, 1), 448_302, 0, 50_566, 397_736),
            projection(LocalDate.of(2026, 12, 1), 397_736, 0, 50_566, 347_170)
        );

        BigDecimal result = simulationItemCalculationService.calculateExpectedPrepMonths(
            projections,
            snapshot
        );

        assertEquals(new BigDecimal("5.35"), result);
    }

    @Test
    @DisplayName("시뮬레이션 기간의 월별 예상 수입과 지출을 순차적으로 생존 기간에 반영한다.")
    void calculatePrepMonthsWithProjectedCashFlow() {
        List<MonthlyProjectionVO> projections = List.of(
            projection(LocalDate.of(2026, 8, 1), 3_000_000, 500_000, 1_000_000, 2_500_000),
            projection(LocalDate.of(2026, 9, 1), 2_500_000, 500_000, 1_000_000, 2_000_000)
        );

        BigDecimal result = simulationItemCalculationService.calculateExpectedPrepMonths(
            projections,
            snapshot
        );

        assertEquals(new BigDecimal("4.00"), result);
    }

    @Test
    @DisplayName("적용 항목이 없는 기준 현금흐름은 현재와 예상 버티는 기간이 같다.")
    void keepCurrentPrepMonthsWithoutAppliedItems() {
        List<MonthlyProjectionVO> projections = List.of(
            projection(LocalDate.of(2026, 8, 1), 5_000_000, 1_000_000, 2_000_000, 4_000_000),
            projection(LocalDate.of(2026, 9, 1), 4_000_000, 1_000_000, 2_000_000, 3_000_000),
            projection(LocalDate.of(2026, 10, 1), 3_000_000, 1_000_000, 2_000_000, 2_000_000)
        );

        BigDecimal result = simulationItemCalculationService.calculateExpectedPrepMonths(
            projections,
            snapshot
        );

        assertEquals(new BigDecimal("5.00"), result);
    }

    @Test
    @DisplayName("월 순현금흐름이 0 이상이면 예상 버티는 기간은 무제한이다.")
    void sustainableCashFlowHasNoFinitePrepMonths() {
        FinancialSnapshotVO sustainableSnapshot = FinancialSnapshotVO.builder()
            .liquidAssets(5_000_000)
            .avgMonthlyIncome(BigDecimal.valueOf(1_000_000))
            .avgMonthlyExpense(BigDecimal.valueOf(500_000))
            .currentPrepMonths(null)
            .build();
        List<MonthlyProjectionVO> projections = List.of(
            projection(LocalDate.of(2026, 8, 1), 5_000_000, 1_000_000, 500_000, 5_500_000)
        );

        BigDecimal result = simulationItemCalculationService.calculateExpectedPrepMonths(
            projections,
            sustainableSnapshot
        );

        assertEquals(new BigDecimal("999.99"), result);
    }

    @Test
    @DisplayName("초기 잔액이 0원이어도 계속 흑자이면 예상 버티는 기간은 무제한이다.")
    void sustainableCashFlowWithoutInitialAssetsHasNoFinitePrepMonths() {
        FinancialSnapshotVO sustainableSnapshot = FinancialSnapshotVO.builder()
            .liquidAssets(0)
            .avgMonthlyIncome(BigDecimal.valueOf(65_666))
            .avgMonthlyExpense(BigDecimal.valueOf(11_633))
            .currentPrepMonths(null)
            .build();
        List<MonthlyProjectionVO> projections = List.of(
            projection(LocalDate.of(2026, 8, 1), 0, 65_666, 11_633, 54_033),
            projection(LocalDate.of(2026, 9, 1), 54_033, 665_666, 11_633, 708_066)
        );

        BigDecimal result = simulationItemCalculationService.calculateExpectedPrepMonths(
            projections,
            sustainableSnapshot
        );

        assertEquals(new BigDecimal("999.99"), result);
    }

    @Test
    @DisplayName("지출 항목을 적용하면 카테고리와 자동 항목명을 저장하고 전체 재정 계획을 재계산한다.")
    void applyMonthlyExpenseItem() {
        ApplySimulationItemRequest request = ApplySimulationItemRequest.builder()
            .category(SimulationItemCategory.EXPENSE)
            .expenseCategory(ExpenseCategory.FOOD)
            .amount(50_000)
            .applyStartDate(LocalDate.of(2026, 8, 15))
            .applyEndDate(LocalDate.of(2026, 9, 30))
            .recurrenceType(SimulationRecurrenceType.MONTHLY)
            .build();

        AtomicReference<SimulationItemVO> savedItem = stubApplyBase();

        ApplySimulationItemResponse result = simulationItemCreateService.applyItem(userId, request);

        assertEquals(1L, PKCrypto.decrypt(result.itemId()));
        assertEquals("식비 줄이기", savedItem.get().getSimulationItemName());
        assertEquals(ExpenseCategory.FOOD, savedItem.get().getSimulationItemExpenseCategory());
        assertEquals(50_000, savedItem.get().getSimulationItemApplyAmount());
        assertNull(savedItem.get().getPolicyId());

        List<MonthlyProjectionVO> savedProjections = captureSavedProjections();
        assertEquals(3, savedProjections.size());
        assertEquals(4_050_000, savedProjections.get(0).getClosingBalance());
        assertEquals(3_100_000, savedProjections.get(1).getClosingBalance());
        assertEquals(2_100_000, savedProjections.get(2).getClosingBalance());

        verify(simulationMapper).updateSummary(100L, 2_100_000, new BigDecimal("5.10"));
    }

    @Test
    @DisplayName("일회성 수입 항목은 시작일이 속한 월에만 반영한다.")
    void applyOnceIncomeItem() {
        ApplySimulationItemRequest request = ApplySimulationItemRequest.builder()
            .category(SimulationItemCategory.INCOME)
            .itemName("단기 알바")
            .amount(300_000)
            .applyStartDate(LocalDate.of(2026, 9, 10))
            .recurrenceType(SimulationRecurrenceType.ONCE)
            .build();

        AtomicReference<SimulationItemVO> savedItem = stubApplyBase();

        ApplySimulationItemResponse result = simulationItemCreateService.applyItem(userId, request);

        assertEquals("단기 알바", savedItem.get().getSimulationItemName());
        assertNull(savedItem.get().getSimulationItemExpenseCategory());
        assertEquals(300_000, savedItem.get().getSimulationItemApplyAmount());

        List<MonthlyProjectionVO> savedProjections = captureSavedProjections();
        assertEquals(4_000_000, savedProjections.get(0).getClosingBalance());
        assertEquals(3_300_000, savedProjections.get(1).getClosingBalance());
        assertEquals(2_300_000, savedProjections.get(2).getClosingBalance());

        assertEquals(1L, PKCrypto.decrypt(result.itemId()));
    }

    @Test
    @DisplayName("정책 항목은 요청 amount를 무시하고 정책 금액과 월 반복 유형으로 저장한다.")
    void applyPolicyItem() {
        ApplySimulationItemRequest request = ApplySimulationItemRequest.builder()
            .category(SimulationItemCategory.POLICY)
            .policyId(7L)
            .amount(999_999)
            .itemName("무시되는 이름")
            .applyStartDate(LocalDate.of(2026, 8, 1))
            .applyEndDate(LocalDate.of(2026, 10, 31))
            .build();

        PolicyVO policy = PolicyVO.builder()
            .policyId(7L)
            .policySupportAmount(200_000)
            .supportMonthCount(12)
            .build();

        given(policyMapper.findById(7L)).willReturn(policy);
        AtomicReference<SimulationItemVO> savedItem = stubApplyBase();

        ApplySimulationItemResponse result = simulationItemCreateService.applyItem(userId, request);

        assertNull(savedItem.get().getSimulationItemName());
        assertNull(savedItem.get().getSimulationItemExpenseCategory());
        assertEquals(7L, savedItem.get().getPolicyId());
        assertEquals(200_000, savedItem.get().getSimulationItemApplyAmount());
        assertNull(savedItem.get().getApplyEndDate());
        assertEquals(SimulationRecurrenceType.MONTHLY, savedItem.get().getRecurrenceType());

        List<MonthlyProjectionVO> savedProjections = captureSavedProjections();
        assertEquals(4_200_000, savedProjections.get(0).getClosingBalance());
        assertEquals(3_400_000, savedProjections.get(1).getClosingBalance());
        assertEquals(2_600_000, savedProjections.get(2).getClosingBalance());

        assertEquals(1L, PKCrypto.decrypt(result.itemId()));
    }

    @Test
    @DisplayName("1개월 지원 정책은 일회성 항목으로 저장한다.")
    void applyOncePolicyItem() {
        ApplySimulationItemRequest request = ApplySimulationItemRequest.builder()
            .category(SimulationItemCategory.POLICY)
            .policyId(7L)
            .applyStartDate(LocalDate.of(2026, 9, 10))
            .build();

        PolicyVO policy = PolicyVO.builder()
            .policyId(7L)
            .policySupportAmount(200_000)
            .supportMonthCount(1)
            .build();

        given(policyMapper.findById(7L)).willReturn(policy);
        AtomicReference<SimulationItemVO> savedItem = stubApplyBase();

        simulationItemCreateService.applyItem(userId, request);

        assertNull(savedItem.get().getApplyEndDate());
        assertEquals(SimulationRecurrenceType.ONCE, savedItem.get().getRecurrenceType());

        List<MonthlyProjectionVO> savedProjections = captureSavedProjections();
        assertEquals(4_000_000, savedProjections.get(0).getClosingBalance());
        assertEquals(3_200_000, savedProjections.get(1).getClosingBalance());
        assertEquals(2_200_000, savedProjections.get(2).getClosingBalance());
    }

    @Test
    @DisplayName("정책 항목은 요청 종료일을 무시하고 정책 지원 개월 수로 기간을 계산한다.")
    void ignoreRequestedEndDateForPolicyItem() {
        ApplySimulationItemRequest request = ApplySimulationItemRequest.builder()
            .category(SimulationItemCategory.POLICY)
            .policyId(7L)
            .applyStartDate(LocalDate.of(2026, 8, 1))
            .applyEndDate(LocalDate.of(2026, 7, 1))
            .build();

        PolicyVO policy = PolicyVO.builder()
            .policyId(7L)
            .policySupportAmount(200_000)
            .supportMonthCount(2)
            .build();

        given(policyMapper.findById(7L)).willReturn(policy);
        AtomicReference<SimulationItemVO> savedItem = stubApplyBase();

        simulationItemCreateService.applyItem(userId, request);

        assertNull(savedItem.get().getApplyEndDate());
        assertEquals(SimulationRecurrenceType.MONTHLY, savedItem.get().getRecurrenceType());
    }

    @Test
    @DisplayName("정책 지원 시작일이 시뮬레이션 기간 전이면 예외가 발생한다.")
    void rejectPolicyStartingBeforeSimulation() {
        given(simulationMapper.findActiveByUserId(userId)).willReturn(simulation);

        ApplySimulationItemRequest request = ApplySimulationItemRequest.builder()
            .category(SimulationItemCategory.POLICY)
            .policyId(7L)
            .applyStartDate(LocalDate.of(2026, 7, 31))
            .build();

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationItemCreateService.applyItem(userId, request)
        );

        assertEquals(SimulationErrorCode.INVALID_SIMULATION_ITEM, exception.getCode());
        verifyNoInteractions(policyMapper);
    }

    @Test
    @DisplayName("시뮬레이션 종료일이 정책 지급일보다 빠르면 종료월 지원금은 반영하지 않는다.")
    void truncatePolicyBeforeRecurrenceDay() {
        simulation.setSimulationDueDate(LocalDate.of(2026, 10, 15));

        ApplySimulationItemRequest request = ApplySimulationItemRequest.builder()
            .category(SimulationItemCategory.POLICY)
            .policyId(7L)
            .applyStartDate(LocalDate.of(2026, 8, 20))
            .build();

        PolicyVO policy = PolicyVO.builder()
            .policyId(7L)
            .policySupportAmount(200_000)
            .supportMonthCount(12)
            .build();

        given(policyMapper.findById(7L)).willReturn(policy);
        AtomicReference<SimulationItemVO> savedItem = stubApplyBase();

        simulationItemCreateService.applyItem(userId, request);

        assertNull(savedItem.get().getApplyEndDate());

        List<MonthlyProjectionVO> savedProjections = captureSavedProjections();
        assertEquals(4_200_000, savedProjections.get(0).getClosingBalance());
        assertEquals(3_400_000, savedProjections.get(1).getClosingBalance());
        assertEquals(2_400_000, savedProjections.get(2).getClosingBalance());
    }

    @Test
    @DisplayName("시뮬레이션 기간을 연장하면 이전에 잘렸던 정책 지원 개월이 다시 반영된다.")
    void restorePolicySupportAfterSimulationExtension() {
        SimulationItemVO policyItem = SimulationItemVO.builder()
            .simulationId(100L)
            .simulationItemCategory(SimulationItemCategory.POLICY)
            .simulationItemApplyAmount(200_000)
            .applyStartDate(LocalDate.of(2026, 8, 20))
            .applyEndDate(null)
            .policyId(7L)
            .recurrenceType(SimulationRecurrenceType.MONTHLY)
            .build();
        simulation.setSimulationDueDate(LocalDate.of(2027, 7, 31));
        given(employmentPreparationMapper.getLivingThresholdByUserId(userId)).willReturn(1_000_000);
        given(policyMapper.findById(7L)).willReturn(PolicyVO.builder()
            .policyId(7L)
            .supportMonthCount(12)
            .build());

        List<MonthlyProjectionVO> projections = simulationItemCalculationService.recalculateProjections(
            simulation,
            snapshot,
            List.of(policyItem)
        );

        assertEquals(12, projections.size());
        assertEquals(1_200_000, projections.get(0).getExpectedIncome());
        assertEquals(1_200_000, projections.get(11).getExpectedIncome());
    }

    @Test
    @DisplayName("계획 기간과 겹치지 않는 항목은 저장하지 않고 예외가 발생한다.")
    void throwWhenApplyPeriodOutOfSimulationPeriod() {
        given(simulationMapper.findActiveByUserId(userId)).willReturn(simulation);

        ApplySimulationItemRequest request = ApplySimulationItemRequest.builder()
            .category(SimulationItemCategory.INCOME)
            .itemName("기간 밖 수입")
            .amount(100_000)
            .applyStartDate(LocalDate.of(2026, 11, 1))
            .recurrenceType(SimulationRecurrenceType.ONCE)
            .build();

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationItemCreateService.applyItem(userId, request)
        );

        assertEquals(SimulationErrorCode.INVALID_SIMULATION_ITEM, exception.getCode());
        verifyNoSave();
    }

    @Test
    @DisplayName("수입 항목 필수값이 없으면 저장하지 않고 예외가 발생한다.")
    void throwWhenIncomeRequiredValueMissing() {
        given(simulationMapper.findActiveByUserId(userId)).willReturn(simulation);

        ApplySimulationItemRequest request = ApplySimulationItemRequest.builder()
            .category(SimulationItemCategory.INCOME)
            .amount(100_000)
            .applyStartDate(LocalDate.of(2026, 8, 1))
            .recurrenceType(SimulationRecurrenceType.MONTHLY)
            .build();

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationItemCreateService.applyItem(userId, request)
        );

        assertEquals(SimulationErrorCode.INVALID_SIMULATION_ITEM, exception.getCode());
        verifyNoSave();
    }

    @Test
    @DisplayName("활성 시뮬레이션이 없으면 예외가 발생한다.")
    void throwWhenActiveSimulationNotFound() {
        given(simulationMapper.findActiveByUserId(userId)).willReturn(null);

        ApplySimulationItemRequest request = incomeMonthlyRequest();

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationItemCreateService.applyItem(userId, request)
        );

        assertEquals(SimulationErrorCode.SIMULATION_NOT_FOUND, exception.getCode());
        verifyNoInteractions(financialSnapshotMapper, simulationItemMapper, monthlyProjectionMapper);
    }

    @Test
    @DisplayName("확정된 시뮬레이션에는 항목을 적용할 수 없다.")
    void throwWhenSimulationAlreadyConfirmed() {
        simulation.setConfirmedAt(LocalDateTime.of(2026, 8, 1, 12, 0));
        given(simulationMapper.findActiveByUserId(userId)).willReturn(simulation);

        ApplySimulationItemRequest request = incomeMonthlyRequest();

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationItemCreateService.applyItem(userId, request)
        );

        assertEquals(SimulationErrorCode.CONFIRMED_SIMULATION_CANNOT_BE_UPDATED, exception.getCode());
        verifyNoInteractions(financialSnapshotMapper, simulationItemMapper, monthlyProjectionMapper);
    }

    @Test
    @DisplayName("최신 스냅샷이 없으면 항목을 저장하지 않는다.")
    void throwWhenSnapshotNotFound() {
        given(simulationMapper.findActiveByUserId(userId)).willReturn(simulation);
        given(financialSnapshotMapper.findLatestByUserId(userId)).willReturn(null);

        ApplySimulationItemRequest request = incomeMonthlyRequest();

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationItemCreateService.applyItem(userId, request)
        );

        assertEquals(AnalysisErrorCode.SNAPSHOT_NOT_FOUND, exception.getCode());
        verify(simulationItemMapper, never()).save(any(SimulationItemVO.class));
        verify(monthlyProjectionMapper, never()).saveAll(any());
    }

    @Test
    @DisplayName("정책이 존재하지 않으면 항목을 저장하지 않는다.")
    void throwWhenPolicyNotFound() {
        given(simulationMapper.findActiveByUserId(userId)).willReturn(simulation);
        given(financialSnapshotMapper.findLatestByUserId(userId)).willReturn(snapshot);
        given(policyMapper.findById(7L)).willReturn(null);

        ApplySimulationItemRequest request = ApplySimulationItemRequest.builder()
            .category(SimulationItemCategory.POLICY)
            .policyId(7L)
            .applyStartDate(LocalDate.of(2026, 8, 1))
            .build();

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationItemCreateService.applyItem(userId, request)
        );

        assertEquals(CatalogErrorCode.POLICY_NOT_FOUND, exception.getCode());
        verify(simulationItemMapper, never()).save(any(SimulationItemVO.class));
        verify(monthlyProjectionMapper, never()).saveAll(any());
    }

    private AtomicReference<SimulationItemVO> stubApplyBase() {
        AtomicReference<SimulationItemVO> savedItem = new AtomicReference<>();

        given(simulationMapper.findActiveByUserId(userId)).willReturn(simulation);
        given(financialSnapshotMapper.findLatestByUserId(userId)).willReturn(snapshot);
        given(employmentPreparationMapper.getLivingThresholdByUserId(userId)).willReturn(1_000_000);
        given(monthlyProjectionMapper.findAllBySimulationId(100L)).willReturn(beforeProjections);
        given(simulationItemMapper.findAllActiveBySimulationId(100L))
            .willAnswer(invocation -> List.of(savedItem.get()));
        doAnswer(invocation -> {
                SimulationItemVO item = invocation.getArgument(0);
                item.setSimulationItemId(1L);
                savedItem.set(item);
                return null;
            })
            .when(simulationItemMapper)
            .save(any(SimulationItemVO.class));

        return savedItem;
    }

    private List<MonthlyProjectionVO> captureSavedProjections() {
        ArgumentCaptor<List<MonthlyProjectionVO>> captor = ArgumentCaptor.forClass(List.class);
        verify(monthlyProjectionMapper).saveAll(captor.capture());
        return captor.getValue();
    }

    private ApplySimulationItemRequest incomeMonthlyRequest() {
        return ApplySimulationItemRequest.builder()
            .category(SimulationItemCategory.INCOME)
            .itemName("정기 알바")
            .amount(100_000)
            .applyStartDate(LocalDate.of(2026, 8, 1))
            .recurrenceType(SimulationRecurrenceType.MONTHLY)
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

    private void verifyNoSave() {
        verify(simulationItemMapper, never()).save(any(SimulationItemVO.class));
        verify(monthlyProjectionMapper, never()).saveAll(any());
        verify(simulationMapper, never()).updateSummary(any(), any(), any());
    }
}
