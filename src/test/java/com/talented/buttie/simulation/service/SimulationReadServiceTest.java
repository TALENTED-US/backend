package com.talented.buttie.simulation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.simulation.domain.MonthlyProjectionVO;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.exception.SimulationErrorCode;
import com.talented.buttie.simulation.mapper.MonthlyProjectionMapper;
import com.talented.buttie.simulation.mapper.SimulationMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SimulationReadServiceTest {

    @Mock
    private SimulationMapper simulationMapper;

    @Mock
    private MonthlyProjectionMapper monthlyProjectionMapper;

    @InjectMocks
    private SimulationReadService simulationReadService;

    // 미확정 시뮬레이션 상세 조회(이어보기)
    @Test
    @DisplayName("성공: 사용자의 최신 미확정 시뮬레이션과 월별 예상 재정 계획을 조회한다.")
    void getActiveSimulation() {
        Long userId = 1L;
        Long simulationId = 10L;
        SimulationVO simulation = SimulationVO.builder()
            .simulationId(simulationId)
            .userId(userId)
            .build();
        List<MonthlyProjectionVO> monthlyProjections = List.of(
            MonthlyProjectionVO.builder()
                .projectionId(100L)
                .simulationId(simulationId)
                .projectionMonth(LocalDate.of(2026, 8, 1))
                .build(),
            MonthlyProjectionVO.builder()
                .projectionId(101L)
                .simulationId(simulationId)
                .projectionMonth(LocalDate.of(2026, 9, 1))
                .build()
        );

        given(simulationMapper.findActiveByUserId(userId))
            .willReturn(simulation);
        given(monthlyProjectionMapper.findAllBySimulationId(simulationId))
            .willReturn(monthlyProjections);

        SimulationVO result =
            simulationReadService.getActiveSimulation(userId);

        assertSame(simulation, result);
        assertEquals(monthlyProjections, result.getMonthlyProjections());
        verify(simulationMapper).findActiveByUserId(userId);
        verify(monthlyProjectionMapper)
            .findAllBySimulationId(simulationId);
    }

    @Test
    @DisplayName("실패: 미확정 시뮬레이션이 없으면 예외가 발생한다.")
    void throwWhenCurrentSimulationNotFound() {
        Long userId = 1L;
        given(simulationMapper.findActiveByUserId(userId))
            .willReturn(null);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationReadService.getActiveSimulation(userId)
        );

        assertEquals(
            SimulationErrorCode.NOT_CONFIRMED_SIMULATION_NOT_FOUND,
            exception.getCode()
        );
        verifyNoInteractions(monthlyProjectionMapper);
    }

    @Test
    @DisplayName("실패: 월별 예상 재정 계획이 없으면 예외가 발생한다.")
    void throwWhenCurrentProjectionNotFound() {
        Long userId = 1L;
        Long simulationId = 10L;
        SimulationVO simulation = SimulationVO.builder()
            .simulationId(simulationId)
            .userId(userId)
            .build();

        given(simulationMapper.findActiveByUserId(userId))
            .willReturn(simulation);
        given(monthlyProjectionMapper.findAllBySimulationId(simulationId))
            .willReturn(List.of());

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationReadService.getActiveSimulation(userId)
        );

        assertEquals(
            SimulationErrorCode.SIMULATION_PROJECTION_NOT_FOUND,
            exception.getCode()
        );
        verify(monthlyProjectionMapper)
            .findAllBySimulationId(simulationId);
    }

    // 최근 확정 시뮬레이션 조회
    @Test
    @DisplayName("성공: 사용자의 최근 확정 시뮬레이션과 월별 예상 재정 계획을 조회한다.")
    void getLatestConfirmedSimulation(){
        Long userId = 1L;
        Long simulationId = 10L;

        SimulationVO simulation = SimulationVO.builder()
            .simulationId(simulationId)
            .userId(userId)
            .build();
        List<MonthlyProjectionVO> monthlyProjections = List.of(
            MonthlyProjectionVO.builder()
                .projectionId(100L)
                .simulationId(simulationId)
                .projectionMonth(LocalDate.of(2026, 8, 1))
                .build()
        );

        given(simulationMapper.findLatestConfirmedByUserId(userId))
            .willReturn(simulation);
        given(monthlyProjectionMapper.findAllBySimulationId(simulationId))
            .willReturn(monthlyProjections);

        SimulationVO result =
            simulationReadService.getLatestConfirmedSimulation(userId);

        assertSame(simulation, result);
        assertEquals(monthlyProjections, result.getMonthlyProjections());
        verify(simulationMapper).findLatestConfirmedByUserId(userId);
        verify(monthlyProjectionMapper)
            .findAllBySimulationId(simulationId);
    }

    @Test
    @DisplayName("실패: 확정 시뮬레이션이 없으면 예외가 발생한다.")
    void throwWhenConfirmedSimulationNotFound(){
        Long userId = 1L;

        given(simulationMapper.findLatestConfirmedByUserId(userId))
            .willReturn(null);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationReadService.getLatestConfirmedSimulation(userId)
        );

        assertEquals(
            SimulationErrorCode.CONFIRMED_SIMULATION_NOT_FOUND,
            exception.getCode()
        );
        verifyNoInteractions(monthlyProjectionMapper);
    }

    @Test
    @DisplayName("실패: 최근 확정 시뮬레이션의 월별 예상 재정 계획이 없으면 예외가 발생한다.")
    void throwWhenConfirmedProjectionNotFound() {
        Long userId = 1L;
        Long simulationId = 10L;

        SimulationVO simulation = SimulationVO.builder()
            .simulationId(simulationId)
            .userId(userId)
            .build();

        given(simulationMapper.findLatestConfirmedByUserId(userId))
            .willReturn(simulation);
        given(monthlyProjectionMapper.findAllBySimulationId(simulationId))
            .willReturn(List.of());

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationReadService.getLatestConfirmedSimulation(userId)
        );

        assertEquals(
            SimulationErrorCode.SIMULATION_PROJECTION_NOT_FOUND,
            exception.getCode()
        );
        verify(monthlyProjectionMapper)
            .findAllBySimulationId(simulationId);
    }
}
