package com.talented.buttie.simulation.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.quest.domain.QuestStatus;
import com.talented.buttie.quest.domain.QuestVO;
import com.talented.buttie.quest.mapper.QuestMapper;
import com.talented.buttie.quest.service.ExperienceService;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.exception.SimulationErrorCode;
import com.talented.buttie.simulation.mapper.SimulationMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SimulationDeleteServiceTest {

    @Mock
    private SimulationMapper simulationMapper;

    @Mock
    private QuestMapper questMapper;

    @Mock
    private ExperienceService experienceService;

    @InjectMocks
    private SimulationDeleteService simulationDeleteService;

    private Long userId;
    private Long simulationId;
    private SimulationVO confirmedSimulation;
    private SimulationVO activeSimulation;

    @BeforeEach
    void setUp() {
        userId = 1L;
        simulationId = 10L;
        confirmedSimulation = SimulationVO.builder()
            .simulationId(simulationId)
            .userId(userId)
            .build();
        activeSimulation = SimulationVO.builder()
            .simulationId(simulationId)
            .userId(userId)
            .build();
    }

    @Test
    @DisplayName("성공: 완료 퀘스트가 없으면 exp 회수 없이 확정 시뮬레이션을 삭제한다.")
    void deleteSimulation() {
        given(simulationMapper.findLatestConfirmedByUserId(userId))
            .willReturn(confirmedSimulation);
        given(questMapper.findAllBySimulationIdForUpdate(simulationId))
            .willReturn(List.of());
        given(simulationMapper.deleteById(simulationId))
            .willReturn(1);

        assertDoesNotThrow(
            () -> simulationDeleteService.deleteSimulation(userId)
        );

        verify(experienceService, never()).deductExperience(any(), any());
        verify(simulationMapper).deleteById(simulationId);
    }

    @Test
    @DisplayName("성공: 완료된 퀘스트가 있으면 지급된 exp를 합산 회수하고 확정 시뮬레이션을 삭제한다.")
    void deleteSimulationWithExpReclaim() {
        // findAllBySimulationIdForUpdate는 SQL에서 QUEST_STATUS = 'COMPLETED'로 이미 필터링해서 반환한다.
        List<QuestVO> quests = List.of(
            quest(QuestStatus.COMPLETED, 100),
            quest(QuestStatus.COMPLETED, 50)
        );
        given(simulationMapper.findLatestConfirmedByUserId(userId))
            .willReturn(confirmedSimulation);
        given(questMapper.findAllBySimulationIdForUpdate(simulationId))
            .willReturn(quests);
        given(simulationMapper.deleteById(simulationId))
            .willReturn(1);

        assertDoesNotThrow(
            () -> simulationDeleteService.deleteSimulation(userId)
        );

        verify(experienceService).deductExperience(userId, 150);
        verify(simulationMapper).deleteById(simulationId);
    }

    @Test
    @DisplayName("실패: 확정 시뮬레이션이 없으면 예외가 발생한다.")
    void throwWhenConfirmedSimulationNotFound() {
        given(simulationMapper.findLatestConfirmedByUserId(userId))
            .willReturn(null);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationDeleteService.deleteSimulation(userId)
        );

        assertEquals(SimulationErrorCode.CONFIRMED_SIMULATION_NOT_FOUND, exception.getCode());
        verify(simulationMapper, never()).deleteById(simulationId);
    }

    @Test
    @DisplayName("실패: 삭제 대상이 존재하지 않으면 예외가 발생한다.")
    void throwWhenDeleteTargetNotFound() {
        given(simulationMapper.findLatestConfirmedByUserId(userId))
            .willReturn(confirmedSimulation);
        given(questMapper.findAllBySimulationIdForUpdate(simulationId))
            .willReturn(List.of());
        given(simulationMapper.deleteById(simulationId))
            .willReturn(0);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationDeleteService.deleteSimulation(userId)
        );

        assertEquals(SimulationErrorCode.CONFIRMED_SIMULATION_NOT_FOUND, exception.getCode());
        verify(simulationMapper).deleteById(simulationId);
    }

    @Test
    @DisplayName("성공: 미확정(활성) 시뮬레이션을 정상적으로 삭제한다.")
    void deleteActiveSimulation() {
        given(simulationMapper.findActiveByUserId(userId))
            .willReturn(activeSimulation);
        given(simulationMapper.deleteActiveById(simulationId))
            .willReturn(1);

        assertDoesNotThrow(
            () -> simulationDeleteService.deleteActiveSimulation(userId)
        );

        verify(simulationMapper).deleteActiveById(simulationId);
    }

    @Test
    @DisplayName("실패: 활성 시뮬레이션이 없으면 예외가 발생한다.")
    void throwWhenActiveSimulationNotFound() {
        given(simulationMapper.findActiveByUserId(userId))
            .willReturn(null);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationDeleteService.deleteActiveSimulation(userId)
        );

        assertEquals(SimulationErrorCode.NOT_CONFIRMED_SIMULATION_NOT_FOUND, exception.getCode());
        verify(simulationMapper, never()).deleteActiveById(simulationId);
    }

    @Test
    @DisplayName("실패: 삭제 대상 활성 시뮬레이션이 존재하지 않으면 예외가 발생한다.")
    void throwWhenActiveDeleteTargetNotFound() {
        given(simulationMapper.findActiveByUserId(userId))
            .willReturn(activeSimulation);
        given(simulationMapper.deleteActiveById(simulationId))
            .willReturn(0);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> simulationDeleteService.deleteActiveSimulation(userId)
        );

        assertEquals(SimulationErrorCode.NOT_CONFIRMED_SIMULATION_NOT_FOUND, exception.getCode());
        verify(simulationMapper).deleteActiveById(simulationId);
    }

    private QuestVO quest(QuestStatus status, Integer expReward) {
        return QuestVO.builder()
            .questStatus(status)
            .expReward(expReward)
            .build();
    }
}
