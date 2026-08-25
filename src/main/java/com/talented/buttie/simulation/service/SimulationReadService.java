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

    // 미확정 시뮬레이션 조회
    @Transactional(readOnly = true)
    public SimulationVO getActiveSimulation(Long userId){
        SimulationVO simulation = simulationMapper.findActiveByUserId(userId);

        if(simulation == null) {
            throw ApplicationException.from(SimulationErrorCode.NOT_CONFIRMED_SIMULATION_NOT_FOUND);
        }

        setMonthlyProjections(simulation);
        return simulation;
    }

    // 최근 확정 시뮬레이션 조회
    @Transactional(readOnly = true)
    public SimulationVO getLatestConfirmedSimulation(Long userId){
        SimulationVO simulation = simulationMapper.findLatestConfirmedByUserId(userId);

        if(simulation == null) {
            throw ApplicationException.from(SimulationErrorCode.CONFIRMED_SIMULATION_NOT_FOUND);
        }

        setMonthlyProjections(simulation);
        return simulation;
    }

    private void setMonthlyProjections(SimulationVO simulation){
        List<MonthlyProjectionVO> monthlyProjections =
            monthlyProjectionMapper.findAllBySimulationId(simulation.getSimulationId());

        if(monthlyProjections.isEmpty()){
            throw ApplicationException.from(SimulationErrorCode.SIMULATION_PROJECTION_NOT_FOUND);
        }

        simulation.setMonthlyProjections(monthlyProjections);
    }
}
