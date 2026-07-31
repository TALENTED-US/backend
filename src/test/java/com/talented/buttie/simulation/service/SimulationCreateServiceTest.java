package com.talented.buttie.simulation.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.simulation.domain.MonthlyProjectionVO;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.dto.request.CreateSimulationRequestDTO;
import com.talented.buttie.simulation.mapper.MonthlyProjectionMapper;
import com.talented.buttie.simulation.mapper.SimulationMapper;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SimulationCreateServiceTest {

    @Mock
    private FinancialSnapshotMapper financialSnapshotMapper;

    @Mock
    private SimulationMapper simulationMapper;

    @Mock
    private MonthlyProjectionMapper monthlyProjectionMapper;

    @Mock
    private ProjectionEngine projectionEngine;

    @Mock
    private EmploymentPreparationMapper employmentPreparationMapper;

    @InjectMocks
    private SimulationCreateService simulationCreateService;

    private Long userId;
    private CreateSimulationRequestDTO request;

    @BeforeEach
    void setup(){
        userId = 1L;
        request = new CreateSimulationRequestDTO(
            LocalDate.of(2026, 8, 1),
            LocalDate.of(2027, 1, 31)
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

        given(financialSnapshotMapper.findLatestByUserId(userId))
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
        verifyNoInteractions(financialSnapshotMapper, employmentPreparationMapper, projectionEngine, monthlyProjectionMapper);
    }

    @Test
    @DisplayName("최신 스냅샷이 없으면 예외가 발생한다.")
    void throwWhenSnapshotNotFound() {
        given(financialSnapshotMapper.findLatestByUserId(userId))
            .willReturn(null);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationCreateService.createSimulation(userId, request)
        );

        assertEquals(AnalysisErrorCode.SNAPSHOT_NOT_FOUND, exception.getCode());
        verify(simulationMapper, never()).save(any(SimulationVO.class));
        verifyNoInteractions(employmentPreparationMapper, projectionEngine, monthlyProjectionMapper);
    }
}
