package com.talented.buttie.simulation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.simulation.domain.FinancialSnapshotVO;
import com.talented.buttie.simulation.domain.MonthlyProjectionVO;
import com.talented.buttie.simulation.domain.SimulationItemCategory;
import com.talented.buttie.simulation.domain.SimulationItemVO;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.exception.AnalysisErrorCode;
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
class SimulationItemDeleteServiceTest {

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
    private SimulationItemDeleteService simulationItemDeleteService;

    private final Long userId = 1L;
    private final Long simulationId = 10L;
    private final Long itemId = 20L;

    private SimulationVO simulation;
    private SimulationItemVO remainingItem;
    private FinancialSnapshotVO snapshot;

    @BeforeEach
    void setUp() {
        simulation = SimulationVO.builder()
            .simulationId(simulationId)
            .userId(userId)
            .snapshotId(100L)
            .simulationStartDate(LocalDate.of(2026, 8, 1))
            .simulationDueDate(LocalDate.of(2026, 12, 31))
            .build();

        remainingItem = SimulationItemVO.builder()
            .simulationItemId(21L)
            .simulationId(simulationId)
            .simulationItemCategory(SimulationItemCategory.EXPENSE)
            .simulationItemApplyAmount(50_000)
            .applyStartDate(LocalDate.of(2026, 8, 1))
            .applyEndDate(LocalDate.of(2026, 10, 31))
            .isDeleted(false)
            .build();

        snapshot = FinancialSnapshotVO.builder()
            .snapshotId(100L)
            .userId(userId)
            .build();
    }

    // 미확정 시뮬레이션 항목 삭제
    @Test
    @DisplayName("성공: 항목 삭제 후 남은 항목 기준으로 에상 재정 계획과 요약을 갱신")
    void deleteItem() {

        List<MonthlyProjectionVO> projections = List.of(
            MonthlyProjectionVO.builder()
                .simulationId(simulationId)
                .projectionMonth(LocalDate.of(2026, 9, 1))
                .closingBalance(1_300_000)
                .build()
        );

        given(simulationMapper.findActiveByUserId(userId))
            .willReturn(simulation);
        given(simulationItemMapper.deleteByIdAndSimulationId(itemId, simulationId))
            .willReturn(1);
        given(financialSnapshotMapper.findById(100L))
            .willReturn(snapshot);
        given(simulationItemMapper.findAllActiveBySimulationId(simulationId))
            .willReturn(List.of(remainingItem));
        given(simulationItemCalculationService.recalculateProjections(simulation, snapshot, List.of(remainingItem)))
            .willReturn(projections);
        given(simulationItemCalculationService.calculateExpectedPrepMonths(projections, snapshot))
            .willReturn(new BigDecimal("4.25"));

        simulationItemDeleteService.deleteItem(userId, itemId);

        verify(simulationItemMapper).deleteByIdAndSimulationId(itemId, simulationId);
        verify(monthlyProjectionMapper).deleteAllBySimulationId(simulationId);
        verify(monthlyProjectionMapper).saveAll(projections);
        verify(simulationMapper).updateSummary(simulationId, 1_300_000, new BigDecimal("4.25"));
    }

    @Test
    @DisplayName("실패: 없거나 삭제된 항목이면 예외가 발생한다.")
    void rejectWhenItemNotFoundOrAlreadyDeleted() {
        given(simulationMapper.findActiveByUserId(userId))
            .willReturn(simulation);
        given(simulationItemMapper.deleteByIdAndSimulationId(itemId, simulationId))
            .willReturn(0);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationItemDeleteService.deleteItem(userId, itemId)
        );

        assertEquals(SimulationErrorCode.SIMULATION_ITEM_NOT_FOUND, exception.getCode());
        verify(monthlyProjectionMapper, never()).deleteAllBySimulationId(simulationId);
    }

    @Test
    @DisplayName("실패: 다른 사용자(시뮬레이션)의 항목이면 예외가 발생한다.")
    void rejectWhenItemBelongsToAnotherSimulation() {

        // simulationId로 스코핑되니 다른 사용자의 항목 ID를 넣어도 매퍼가 0반환
        given(simulationMapper.findActiveByUserId(userId)).willReturn(simulation);
        given(simulationItemMapper.deleteByIdAndSimulationId(itemId, simulationId)).willReturn(0);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationItemDeleteService.deleteItem(userId, itemId)
        );

        assertEquals(SimulationErrorCode.SIMULATION_ITEM_NOT_FOUND, exception.getCode());
        verify(simulationItemMapper).deleteByIdAndSimulationId(itemId, simulationId);
    }

    @Test
    @DisplayName("실패: 확정된 시뮬레이션의 항목은 삭제할 수 없다")
    void rejectWhenSimulationAlreadyConfirmed() {
        given(simulationMapper.findActiveByUserId(userId)).willReturn(null);

        given(simulationMapper.findLatestConfirmedByUserId(userId)).willReturn(simulation);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationItemDeleteService.deleteItem(userId, itemId)
        );

        assertEquals(SimulationErrorCode.CONFIRMED_SIMULATION_CANNOT_BE_UPDATED, exception.getCode());
        verify(simulationItemMapper, never()).deleteByIdAndSimulationId(itemId, simulationId);
    }

    @Test
    @DisplayName("실패: 미확정/확정 시뮬레이션이 모두 없으면 예외가 발생한다")
    void rejectWhenNoSimulationExists() {
        given(simulationMapper.findActiveByUserId(userId)).willReturn(null);

        given(simulationMapper.findLatestConfirmedByUserId(userId)).willReturn(null);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationItemDeleteService.deleteItem(userId, itemId)
        );

        assertEquals(SimulationErrorCode.NOT_CONFIRMED_SIMULATION_NOT_FOUND,
            exception.getCode());
        verify(simulationItemMapper, never()).deleteByIdAndSimulationId(itemId,
            simulationId);
    }

    @Test
    @DisplayName("실패: 최신 스냅샷이 없으면 재계산할 수 없다")
    void rejectWhenSnapshotNotFound() {
        given(simulationMapper.findActiveByUserId(userId)).willReturn(simulation);
        given(simulationItemMapper.deleteByIdAndSimulationId(itemId, simulationId))
            .willReturn(1);
        given(financialSnapshotMapper.findById(100L)).willReturn(null);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationItemDeleteService.deleteItem(userId, itemId)
        );

        assertEquals(AnalysisErrorCode.SNAPSHOT_NOT_FOUND, exception.getCode());
        verify(monthlyProjectionMapper, never())
            .saveAll(org.mockito.ArgumentMatchers.anyList());
    }
}