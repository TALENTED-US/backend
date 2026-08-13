package com.talented.buttie.simulation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.simulation.domain.FinancialSnapshotVO;
import com.talented.buttie.simulation.domain.MonthlyProjectionVO;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.dto.request.CreateSimulationRequest;
import com.talented.buttie.simulation.exception.AnalysisErrorCode;
import com.talented.buttie.simulation.exception.SimulationErrorCode;
import com.talented.buttie.simulation.mapper.MonthlyProjectionMapper;
import com.talented.buttie.simulation.mapper.SimulationMapper;
import com.talented.buttie.simulation.service.SimulationItemReadService.FinancialSnapshotCreateService;
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
    private SimulationMapper simulationMapper;

    @Mock
    private MonthlyProjectionMapper monthlyProjectionMapper;

    @Spy
    private ProjectionEngine projectionEngine = new ProjectionEngine();

    @Mock
    private EmploymentPreparationMapper employmentPreparationMapper;

    private SimulationCreateService simulationCreateService;

    private Long userId;
    private CreateSimulationRequest createRequest;
    private SimulationVO activeSimulation;

    @BeforeEach
    void setup() {
        simulationCreateService = new SimulationCreateService(
            financialSnapshotCreateService,
            simulationMapper,
            monthlyProjectionMapper,
            projectionEngine,
            employmentPreparationMapper
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

    }

    @Test
    @DisplayName("성공: 시뮬레이션 최초 생성 시 스냅샷을 생성하고 예상 재정 계획도 생성한다.")
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
            .currentPrepMonths(BigDecimal.valueOf(8.25))
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

    // 시뮬레이션 생성
    @Test
    @DisplayName("실패: 기존 미확정 시뮬레이션이 있으면 예외가 발생한다.")
    void rejectWhenActiveSimulationExists() {
        given(simulationMapper.findActiveByUserId(userId)).willReturn(activeSimulation);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationCreateService.createSimulation(userId, createRequest)
        );

        assertEquals(SimulationErrorCode.ALREADY_NOT_CONFIRMED_SIMULATION_EXISTS, exception.getCode());
        verify(simulationMapper, never()).save(any(SimulationVO.class));
        verifyNoInteractions(
            financialSnapshotCreateService,
            employmentPreparationMapper,
            projectionEngine,
            monthlyProjectionMapper
        );
    }

    @Test
    @DisplayName("실패: 기존 확정 시뮬레이션이 있으면 예외가 발생한다.")
    void rejectWhenConfirmedSimulationExists() {
        given(simulationMapper.findActiveByUserId(userId)).willReturn(null);
        given(simulationMapper.findLatestConfirmedByUserId(userId)).willReturn(activeSimulation);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationCreateService.createSimulation(userId, createRequest)
        );

        assertEquals(SimulationErrorCode.ALREADY_CONFIRMED_SIMULATION_EXISTS, exception.getCode());
        verify(simulationMapper, never()).save(any(SimulationVO.class));
        verifyNoInteractions(
            financialSnapshotCreateService,
            employmentPreparationMapper,
            projectionEngine,
            monthlyProjectionMapper
        );
    }

    @Test
    @DisplayName("실패: 스냅샷 생성에 실패하면 예외가 전파된다.")
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
