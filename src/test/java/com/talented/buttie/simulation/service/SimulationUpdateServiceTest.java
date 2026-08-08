package com.talented.buttie.simulation.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.simulation.domain.MonthlyProjectionVO;
import com.talented.buttie.simulation.domain.SimulationItemVO;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.dto.request.UpdateSimulationPeriodRequest;
import com.talented.buttie.simulation.exception.SimulationErrorCode;
import com.talented.buttie.simulation.mapper.MonthlyProjectionMapper;
import com.talented.buttie.simulation.mapper.SimulationItemMapper;
import com.talented.buttie.simulation.mapper.SimulationMapper;
import com.talented.buttie.snapshot.domain.FinancialSnapshotVO;
import com.talented.buttie.snapshot.exception.AnalysisErrorCode;
import com.talented.buttie.snapshot.mapper.FinancialSnapshotMapper;
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
class SimulationUpdateServiceTest {

    @Mock
    private SimulationMapper simulationMapper;

    @Mock
    private MonthlyProjectionMapper monthlyProjectionMapper;

    @Mock
    private SimulationItemMapper simulationItemMapper;

    @Mock
    private FinancialSnapshotMapper financialSnapshotMapper;

    @Mock
    private SimulationItemCalculationService simulationItemCalculationService;

    @InjectMocks
    private SimulationUpdateService simulationUpdateService;

    private Long userId;
    private Long simulationId;
    private UpdateSimulationPeriodRequest request;
    private SimulationVO activeSimulation;
    private FinancialSnapshotVO snapshot;
    private List<SimulationItemVO> appliedItems;
    private List<MonthlyProjectionVO> recalculatedProjections;

    @BeforeEach
    void setUp() {
        userId = 1L;
        simulationId = 10L;
        request = new UpdateSimulationPeriodRequest(
            LocalDate.of(2026, 8, 1),
            LocalDate.of(2027, 1, 31)
        );
        activeSimulation = SimulationVO.builder()
            .simulationId(simulationId)
            .userId(userId)
            .snapshotId(100L)
            .build();
        snapshot = FinancialSnapshotVO.builder().snapshotId(100L).userId(userId).build();
        appliedItems = List.of(SimulationItemVO.builder().simulationItemId(1L).build());
        recalculatedProjections = List.of(
            projection(LocalDate.of(2026, 8, 1), 1_000_000, 2_000_000),
            projection(LocalDate.of(2026, 9, 1), 2_000_000, 3_000_000)
        );
    }

    // 미확정 시뮬레이션 수행 기간 수정
    @Test
    @DisplayName("성공: 시뮬레이션 기간을 정상적으로 수정한다.")
    void updateSimulationPeriod() {
        given(simulationMapper.findActiveByUserId(userId))
            .willReturn(activeSimulation);
        given(financialSnapshotMapper.findById(100L)).willReturn(snapshot);
        given(simulationItemMapper.findAllActiveBySimulationId(simulationId)).willReturn(appliedItems);
        given(simulationItemCalculationService.recalculateProjections(activeSimulation, snapshot, appliedItems))
            .willReturn(recalculatedProjections);
        given(simulationItemCalculationService.calculateExpectedPrepMonths(recalculatedProjections, snapshot))
            .willReturn(new BigDecimal("3.00"));
        given(simulationMapper.updateSimulationPeriod(
            simulationId, request.simulationStartDate(), request.simulationDueDate(), 3_000_000
        )).willReturn(1);

        assertDoesNotThrow(
            () -> simulationUpdateService.updateSimulationPeriod(userId, request)
        );

        verify(simulationMapper).updateSimulationPeriod(
            simulationId, request.simulationStartDate(), request.simulationDueDate(), 3_000_000
        );
        verify(monthlyProjectionMapper)
            .deleteAllBySimulationId(simulationId);
        verify(monthlyProjectionMapper)
            .saveAll(recalculatedProjections);
        verify(simulationMapper).updateSummary(simulationId, 3_000_000, new BigDecimal("3.00"));
    }

    @Test
    @DisplayName("실패: 시뮬레이션 기간이 올바르지 않으면 예외가 발생한다.")
    void throwWhenPeriodIsInvalid() {
        UpdateSimulationPeriodRequest errorRequest = UpdateSimulationPeriodRequest.builder()
            .simulationStartDate(LocalDate.of(2027, 1, 1))
            .simulationDueDate(LocalDate.of(2026, 1, 1))
            .build();

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationUpdateService.updateSimulationPeriod(userId, errorRequest)
        );

        assertEquals(SimulationErrorCode.INVALID_SIMULATION_PERIOD, exception.getCode());
        verifyNoInteractions(
            simulationMapper,
            simulationItemMapper,
            monthlyProjectionMapper,
            financialSnapshotMapper,
            simulationItemCalculationService
        );
    }

    @Test
    @DisplayName("실패: 시뮬레이션에 연결된 스냅샷이 없으면 예외가 발생한다.")
    void throwWhenSnapshotNotFound() {
        given(simulationMapper.findActiveByUserId(userId))
            .willReturn(activeSimulation);
        given(financialSnapshotMapper.findById(100L)).willReturn(null);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationUpdateService.updateSimulationPeriod(userId, request)
        );

        assertEquals(AnalysisErrorCode.SNAPSHOT_NOT_FOUND, exception.getCode());
        verifyNoInteractions(simulationItemMapper, monthlyProjectionMapper, simulationItemCalculationService);
    }

    @Test
    @DisplayName("실패: 확정된 시뮬레이션의 기간을 수정하면 예외가 발생한다.")
    void throwWhenConfirmedSimulationIsUpdated() {
        given(simulationMapper.findActiveByUserId(userId))
            .willReturn(null);
        given(simulationMapper.findLatestConfirmedByUserId(userId))
            .willReturn(activeSimulation);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationUpdateService.updateSimulationPeriod(userId, request)
        );

        assertEquals(
            SimulationErrorCode.CONFIRMED_SIMULATION_CANNOT_BE_UPDATED,
            exception.getCode()
        );
        verifyNoInteractions(
            simulationItemMapper,
            monthlyProjectionMapper,
            financialSnapshotMapper,
            simulationItemCalculationService
        );
    }

    @Test
    @DisplayName("실패: 수정할 시뮬레이션이 없으면 예외가 발생한다.")
    void throwWhenSimulationNotFound() {
        given(simulationMapper.findActiveByUserId(userId))
            .willReturn(null);
        given(simulationMapper.findLatestConfirmedByUserId(userId))
            .willReturn(null);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationUpdateService.updateSimulationPeriod(userId, request)
        );

        assertEquals(SimulationErrorCode.NOT_CONFIRMED_SIMULATION_NOT_FOUND, exception.getCode());
        verifyNoInteractions(
            simulationItemMapper,
            monthlyProjectionMapper,
            financialSnapshotMapper,
            simulationItemCalculationService
        );
    }

    @Test
    @DisplayName("성공: 기간 수정 후 예상 재정 계획을 다시 생성한다.")
    void recreateProjectionsAfterPeriodUpdate() {
        given(simulationMapper.findActiveByUserId(userId))
            .willReturn(activeSimulation);
        given(financialSnapshotMapper.findById(100L)).willReturn(snapshot);
        given(simulationItemMapper.findAllActiveBySimulationId(simulationId)).willReturn(appliedItems);
        given(simulationItemCalculationService.recalculateProjections(activeSimulation, snapshot, appliedItems))
            .willReturn(recalculatedProjections);
        given(simulationItemCalculationService.calculateExpectedPrepMonths(recalculatedProjections, snapshot))
            .willReturn(new BigDecimal("3.00"));
        given(simulationMapper.updateSimulationPeriod(
            simulationId, request.simulationStartDate(), request.simulationDueDate(), 3_000_000
        )).willReturn(1);

        simulationUpdateService.updateSimulationPeriod(userId, request);

        verify(simulationItemCalculationService)
            .recalculateProjections(activeSimulation, snapshot, appliedItems);
        verify(monthlyProjectionMapper).deleteAllBySimulationId(simulationId);
        verify(monthlyProjectionMapper).saveAll(recalculatedProjections);
    }

    private MonthlyProjectionVO projection(
        LocalDate projectionMonth,
        int openingBalance,
        int closingBalance
    ) {
        return MonthlyProjectionVO.builder()
            .simulationId(simulationId)
            .projectionMonth(projectionMonth)
            .openingBalance(openingBalance)
            .expectedIncome(3_000_000)
            .expectedExpense(2_000_000)
            .closingBalance(closingBalance)
            .adjustmentRequired(false)
            .build();
    }
}
