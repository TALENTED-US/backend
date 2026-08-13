package com.talented.buttie.simulation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.simulation.domain.FinancialSnapshotVO;
import com.talented.buttie.simulation.domain.MonthlyProjectionVO;
import com.talented.buttie.simulation.domain.SimulationItemCategory;
import com.talented.buttie.simulation.domain.SimulationItemVO;
import com.talented.buttie.simulation.domain.SimulationRecurrenceType;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.dto.request.UpdateSimulationItemRequest;
import com.talented.buttie.simulation.dto.response.simulation.SimulationItemResponse;
import com.talented.buttie.simulation.exception.SimulationErrorCode;
import com.talented.buttie.simulation.mapper.FinancialSnapshotMapper;
import com.talented.buttie.simulation.mapper.MonthlyProjectionMapper;
import com.talented.buttie.simulation.mapper.SimulationItemMapper;
import com.talented.buttie.simulation.mapper.SimulationMapper;
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

@ExtendWith(MockitoExtension.class)
class SimulationItemUpdateServiceTest {

    @Mock
    private SimulationMapper simulationMapper;

    @Mock
    private SimulationItemMapper simulationItemMapper;

    @Mock
    private MonthlyProjectionMapper monthlyProjectionMapper;

    @Mock
    private FinancialSnapshotMapper financialSnapshotMapper;

    @Mock
    private SimulationItemCalculationService simulationItemCalculationService;

    @InjectMocks
    private SimulationItemUpdateService simulationItemUpdateService;

    private final Long userId = 1L;
    private final Long simulationId = 10L;
    private final Long itemId = 20L;

    private SimulationVO simulation;
    private SimulationItemVO incomeItem;
    private SimulationItemVO expenseItem;
    private FinancialSnapshotVO snapshot;

    @BeforeEach
    void setUp() {
        PKCrypto crypto = new PKCrypto("AES", "1234567890123456");
        crypto.init();

        simulation = SimulationVO.builder()
            .simulationId(simulationId)
            .userId(userId)
            .snapshotId(100L)
            .simulationStartDate(LocalDate.of(2026, 8, 1))
            .simulationDueDate(LocalDate.of(2026, 12, 31))
            .build();

        incomeItem = SimulationItemVO.builder()
            .simulationItemId(itemId)
            .simulationId(simulationId)
            .simulationItemCategory(SimulationItemCategory.INCOME)
            .simulationItemName("기존 수입")
            .simulationItemApplyAmount(100_000)
            .applyStartDate(LocalDate.of(2026, 8, 1))
            .applyEndDate(LocalDate.of(2026, 10, 31))
            .recurrenceType(SimulationRecurrenceType.MONTHLY)
            .isDeleted(false)
            .build();

        expenseItem = SimulationItemVO.builder()
            .simulationItemId(itemId)
            .simulationId(simulationId)
            .simulationItemCategory(SimulationItemCategory.EXPENSE)
            .simulationItemApplyAmount(50_000)
            .applyStartDate(LocalDate.of(2026, 8, 1))
            .applyEndDate(LocalDate.of(2026, 10, 31))
            .recurrenceType(SimulationRecurrenceType.MONTHLY)
            .isDeleted(false)
            .build();

        snapshot = FinancialSnapshotVO.builder()
            .snapshotId(100L)
            .userId(userId)
            .build();
    }

    private void stubRecalculation(SimulationItemVO item, List<MonthlyProjectionVO> projections) {
        given(simulationMapper.findActiveByUserId(userId)).willReturn(simulation);
        given(simulationItemMapper.findActiveByIdAndSimulationId(itemId, simulationId)).willReturn(item);
        given(simulationItemMapper.update(item)).willReturn(1);
        given(financialSnapshotMapper.findById(100L)).willReturn(snapshot);
        given(simulationItemMapper.findAllActiveBySimulationId(simulationId)).willReturn(List.of(item));
        given(simulationItemCalculationService.recalculateProjections(simulation, snapshot, List.of(item)))
            .willReturn(projections);
        given(simulationItemCalculationService.calculateExpectedPrepMonths(projections, snapshot))
            .willReturn(new BigDecimal("4.25"));
    }

    // 미확정 시뮬레이션 항목 조건 수정
    @Test
    @DisplayName("성공: 수입 항목 수정 후 전체 예상 재정 계획과 요약을 갱신")
    void updateIncomeItemAndRecalculateSimulation() {
        UpdateSimulationItemRequest request =
            UpdateSimulationItemRequest.builder()
                .itemName("수정 수입")
                .amount(300_000)
                .applyStartDate(LocalDate.of(2026, 9, 1))
                .applyEndDate(LocalDate.of(2026, 11, 30))
                .recurrenceType(SimulationRecurrenceType.MONTHLY)
                .build();
        List<MonthlyProjectionVO> projections = List.of(
            MonthlyProjectionVO.builder()
                .simulationId(simulationId)
                .projectionMonth(LocalDate.of(2026, 9, 1))
                .closingBalance(1_300_000)
                .build()
        );
        stubRecalculation(incomeItem, projections);

        SimulationItemResponse result = simulationItemUpdateService.updateItem(userId, itemId, request);

        assertEquals("수정 수입", result.displayName());
        assertEquals(300_000, result.amount());
        assertEquals(LocalDate.of(2026, 11, 30), result.applyEndDate());

        verify(monthlyProjectionMapper).deleteAllBySimulationId(simulationId);
        verify(monthlyProjectionMapper).saveAll(projections);
        verify(simulationMapper).updateSummary(simulationId, 1_300_000, new BigDecimal("4.25"));
    }

    @Test
    @DisplayName("성공: 지출 항목 수정 시 항목 이름 없이 처리")
    void updateExpenseItem() {
        UpdateSimulationItemRequest request =
            UpdateSimulationItemRequest.builder()
                .amount(80_000)
                .applyStartDate(LocalDate.of(2026, 9, 1))
                .applyEndDate(LocalDate.of(2026, 11, 30))
                .recurrenceType(SimulationRecurrenceType.MONTHLY)
                .build();
        List<MonthlyProjectionVO> projections = List.of(
            MonthlyProjectionVO.builder()
                .simulationId(simulationId)
                .projectionMonth(LocalDate.of(2026, 9, 1))
                .closingBalance(900_000)
                .build()
        );
        stubRecalculation(expenseItem, projections);

        SimulationItemResponse result = simulationItemUpdateService.updateItem(userId, itemId, request);

        assertEquals(80_000, result.amount());
    }

    @Test
    @DisplayName("성공: ONCE 항목은 종료일 생략 시 시작일과 동일하게 설정")
    void onceItemDefaultsEndDateToStartDate() {
        UpdateSimulationItemRequest request =
            UpdateSimulationItemRequest.builder()
                .itemName("단발성 수입")
                .amount(200_000)
                .applyStartDate(LocalDate.of(2026, 9, 10))
                .recurrenceType(SimulationRecurrenceType.ONCE)
                .build();
        List<MonthlyProjectionVO> projections = List.of(
            MonthlyProjectionVO.builder()
                .simulationId(simulationId)
                .projectionMonth(LocalDate.of(2026, 9, 1))
                .closingBalance(1_000_000)
                .build()
        );
        stubRecalculation(incomeItem, projections);

        SimulationItemResponse result = simulationItemUpdateService.updateItem(userId, itemId, request);

        assertEquals(LocalDate.of(2026, 9, 10), result.applyStartDate());
    }

    @Test
    @DisplayName("실패: MONTHLY인데 종료일이 없으면 예외")
    void rejectMonthlyWithoutEndDate() {
        given(simulationMapper.findActiveByUserId(userId)).willReturn(simulation);
        given(simulationItemMapper.findActiveByIdAndSimulationId(itemId, simulationId)).willReturn(incomeItem);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationItemUpdateService.updateItem(userId, itemId,
                UpdateSimulationItemRequest.builder()
                    .itemName("수입")
                    .amount(200_000)
                    .applyStartDate(LocalDate.of(2026, 9, 1))
                    .recurrenceType(SimulationRecurrenceType.MONTHLY)
                    .build())
        );

        assertEquals(SimulationErrorCode.INVALID_SIMULATION_ITEM_APPLY_PERIOD, exception.getCode());
    }

    @Test
    @DisplayName("실패: 수입 항목인데 이름이 없으면 예외")
    void rejectIncomeItemWithoutName() {
        given(simulationMapper.findActiveByUserId(userId)).willReturn(simulation);
        given(simulationItemMapper.findActiveByIdAndSimulationId(itemId, simulationId)).willReturn(incomeItem);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationItemUpdateService.updateItem(userId, itemId,
                UpdateSimulationItemRequest.builder()
                    .amount(200_000)
                    .applyStartDate(LocalDate.of(2026, 9, 1))
                    .applyEndDate(LocalDate.of(2026, 11, 30))
                    .recurrenceType(SimulationRecurrenceType.MONTHLY)
                    .build())
        );

        assertEquals(SimulationErrorCode.INVALID_INCOME_ITEM_NAME, exception.getCode());
    }

    @Test
    @DisplayName("실패: 지출 항목인데 이름을 보내면 예외")
    void rejectExpenseItemWithName() {
        given(simulationMapper.findActiveByUserId(userId)).willReturn(simulation);
        given(simulationItemMapper.findActiveByIdAndSimulationId(itemId, simulationId)).willReturn(expenseItem);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationItemUpdateService.updateItem(userId, itemId,
                UpdateSimulationItemRequest.builder()
                    .itemName("식비")
                    .amount(80_000)
                    .applyStartDate(LocalDate.of(2026, 9, 1))
                    .applyEndDate(LocalDate.of(2026, 11, 30))
                    .recurrenceType(SimulationRecurrenceType.MONTHLY)
                    .build())
        );

        assertEquals(SimulationErrorCode.ITEM_NAME_NOT_ALLOWED_FOR_EXPENSE, exception.getCode());
    }

    @Test
    @DisplayName("실패: 정책 항목은 수정 불가")
    void rejectPolicyItemUpdate() {
        SimulationItemVO policyItem = SimulationItemVO.builder()
            .simulationItemId(itemId)
            .simulationId(simulationId)
            .simulationItemCategory(SimulationItemCategory.POLICY)
            .build();
        given(simulationMapper.findActiveByUserId(userId))
            .willReturn(simulation);
        given(simulationItemMapper.findActiveByIdAndSimulationId(itemId, simulationId))
            .willReturn(policyItem);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationItemUpdateService.updateItem(userId, itemId,
                UpdateSimulationItemRequest.builder()
                    .applyStartDate(LocalDate.of(2026, 9, 1))
                    .build())
        );
        assertEquals(SimulationErrorCode.POLICY_ITEM_CANNOT_BE_UPDATED, exception.getCode());
        verify(simulationItemMapper, never()).update(policyItem);
    }

    @Test
    @DisplayName("실패: 없거나 or 삭제 or 다른 사용자의 항목 수정 불가")
    void rejectItemNotOwnedByActiveSimulation() {
        given(simulationMapper.findActiveByUserId(userId))
            .willReturn(simulation);
        given(simulationItemMapper.findActiveByIdAndSimulationId(itemId, simulationId))
            .willReturn(null);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationItemUpdateService.updateItem(userId, itemId,
                UpdateSimulationItemRequest.builder().amount(200_000).build())
        );

        assertEquals(SimulationErrorCode.SIMULATION_ITEM_NOT_FOUND, exception.getCode());
    }

    @Test
    @DisplayName("실패: 필수 값이 없으면 요청 거부")
    void rejectMissingRequiredFields() {
        given(simulationMapper.findActiveByUserId(userId))
            .willReturn(simulation);
        given(simulationItemMapper.findActiveByIdAndSimulationId(itemId, simulationId))
            .willReturn(incomeItem);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationItemUpdateService.updateItem(userId, itemId,
                UpdateSimulationItemRequest.builder().build())
        );

        assertEquals(SimulationErrorCode.INVALID_SIMULATION_ITEM, exception.getCode());
    }

    @Test
    @DisplayName("실패: 확정된 시뮬레이션 수정 불가")
    void rejectInvalidApplyPeriod() {
        given(simulationMapper.findActiveByUserId(userId)).willReturn(null);
        given(simulationMapper.findLatestConfirmedByUserId(userId))
            .willReturn(simulation);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationItemUpdateService.updateItem(userId, itemId,
                UpdateSimulationItemRequest.builder().amount(200_000).build()
            )
        );

        assertEquals(SimulationErrorCode.CONFIRMED_SIMULATION_CANNOT_BE_UPDATED, exception.getCode());
    }
}
