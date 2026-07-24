package com.talented.buttie.simulation.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.dto.request.CreateSimulationRequest;
import com.talented.buttie.simulation.mapper.SimulationMapper;
import com.talented.buttie.snapshot.domain.FinancialSnapshotVO;
import com.talented.buttie.snapshot.exception.AnalysisErrorCode;
import com.talented.buttie.snapshot.mapper.FinancialSnapshotMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SimulationPostServiceTest {

    @Mock
    private FinancialSnapshotMapper financialSnapshotMapper;

    @Mock
    private SimulationMapper simulationMapper;

    @InjectMocks
    private SimulationPostService simulationPostService;

    private Long userId;
    private CreateSimulationRequest request;

    @BeforeEach
    void setup(){
        userId = 1L;
        request = new CreateSimulationRequest(
            LocalDateTime.of(2026, 8, 1, 0, 0),
            LocalDateTime.of(2027, 1, 31, 0, 0)
        );
    }

    @Test
    @DisplayName("시뮬레이션 최초 생성 테스트")
    void createSimulation() {
        FinancialSnapshotVO snapshot = FinancialSnapshotVO.builder()
            .snapshotId(10L)
            .userId(userId)
            .liquidAssets(5_000_000)
            .prepPossibleMonths(new BigDecimal("8.25"))
            .targetAchievementRate(new BigDecimal("35.50"))
            .build();

        given(financialSnapshotMapper.findLatestByUserId(userId))
            .willReturn(snapshot);

        // when: 실제 테스트 실행
        SimulationVO result = simulationPostService.createSimulation(userId, request);

        // then: 결과 확인
        assertEquals(5_000_000, result.getEndingBalance());
        verify(simulationMapper).save(result);
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

        SimulationVO result = simulationPostService.createSimulation(userId, request);

        assertSame(activeSimulation, result);
        verify(simulationMapper, never()).save(any(SimulationVO.class));
        verifyNoInteractions(financialSnapshotMapper);
    }

    @Test
    @DisplayName("최신 스냅샷이 없으면 예외가 발생한다.")
    void throwWhenSnapshotNotFound() {
        given(financialSnapshotMapper.findLatestByUserId(userId))
            .willReturn(null);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationPostService.createSimulation(userId, request)
        );

        assertEquals(AnalysisErrorCode.SNAPSHOT_NOT_FOUND, exception.getCode());
        verify(simulationMapper, never()).save(any(SimulationVO.class));
    }
}
