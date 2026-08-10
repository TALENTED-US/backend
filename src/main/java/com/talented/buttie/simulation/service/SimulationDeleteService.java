package com.talented.buttie.simulation.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.quest.domain.QuestStatus;
import com.talented.buttie.quest.domain.QuestVO;
import com.talented.buttie.quest.mapper.QuestMapper;
import com.talented.buttie.quest.service.ExperienceService;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.exception.SimulationErrorCode;
import com.talented.buttie.simulation.mapper.SimulationMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SimulationDeleteService {

    private final SimulationMapper simulationMapper;
    private final QuestMapper questMapper;
    private final ExperienceService experienceService;

    @Transactional
    public void deleteSimulation(Long userId) {
        SimulationVO confirmedSimulation = simulationMapper.findLatestConfirmedByUserId(userId);

        if(confirmedSimulation == null) {
            throw ApplicationException.from(SimulationErrorCode.CONFIRMED_SIMULATION_NOT_FOUND);
        }

        List<QuestVO> quests = questMapper.findAllBySimulationId(confirmedSimulation.getSimulationId());

        int totalExp = quests.stream()
            .filter(q -> q.getQuestStatus() == QuestStatus.COMPLETED)
            .mapToInt(q -> q.getExpReward() == null ? 0 : q.getExpReward())
            .sum();

        if(totalExp > 0) {
            experienceService.deductExperience(userId, totalExp);
        }

        int deletedrows = simulationMapper.deleteById(confirmedSimulation.getSimulationId());

        if(deletedrows == 0) {
            throw ApplicationException.from(SimulationErrorCode.CONFIRMED_SIMULATION_NOT_FOUND);
        }
    }

    public void deleteActiveSimulation(Long userId) {
        SimulationVO activeSimulation = simulationMapper.findActiveByUserId(userId);

        if(activeSimulation == null) {
            throw ApplicationException.from(SimulationErrorCode.NOT_CONFIRMED_SIMULATION_NOT_FOUND);
        }

        int updatedRows = simulationMapper.deleteActiveById(activeSimulation.getSimulationId());

        if(updatedRows == 0) {
            throw ApplicationException.from(SimulationErrorCode.NOT_CONFIRMED_SIMULATION_NOT_FOUND);
        }
    }
}
