package com.talented.buttie.simulation.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.simulation.domain.MonthlyProjectionVO;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.exception.SimulationErrorCode;
import com.talented.buttie.simulation.mapper.MonthlyProjectionMapper;
import com.talented.buttie.simulation.mapper.SimulationMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SimulationReadService {

    private final SimulationMapper simulationMapper;
    private final MonthlyProjectionMapper monthlyProjectionMapper;

    @Transactional(readOnly = true)
    public SimulationVO getSimulationDetail(Long userId){
        SimulationVO simulation = simulationMapper.findActiveByUserId(userId);

        if(simulation == null){
            throw ApplicationException.from(SimulationErrorCode.SIMULATION_NOT_FOUND);
        }

        Long simulationId = simulation.getSimulationId();

        List<MonthlyProjectionVO> monthlyProjections = monthlyProjectionMapper.findAllBySimulationId(simulationId);

        if(monthlyProjections.isEmpty()){
            throw ApplicationException.from(SimulationErrorCode.SIMULATION_PROJECTION_NOT_FOUND);
        }

        simulation.setMonthlyProjections(monthlyProjections);

        return simulation;
    }
}
