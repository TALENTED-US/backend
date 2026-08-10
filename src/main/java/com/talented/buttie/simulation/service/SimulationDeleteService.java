package com.talented.buttie.simulation.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.exception.SimulationErrorCode;
import com.talented.buttie.simulation.mapper.SimulationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SimulationDeleteService {

    private final SimulationMapper simulationMapper;

    @Transactional
    public void deleteSimulation(Long userId) {
        SimulationVO confirmedSimulation = simulationMapper.findLatestConfirmedByUserId(userId);

        if(confirmedSimulation == null) {
            throw ApplicationException.from(SimulationErrorCode.CONFIRMED_SIMULATION_NOT_FOUND);
        }

        int deleterows = simulationMapper.deleteById(confirmedSimulation.getSimulationId());

        if(deleterows == 0) {
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
