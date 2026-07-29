package com.talented.buttie.simulation.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.dto.request.CreateSimulationRequestDTO;
import com.talented.buttie.simulation.mapper.SimulationMapper;
import com.talented.buttie.snapshot.domain.FinancialSnapshotVO;
import com.talented.buttie.snapshot.exception.AnalysisErrorCode;
import com.talented.buttie.snapshot.mapper.FinancialSnapshotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SimulationCreateService {

    private final FinancialSnapshotMapper financialSnapshotMapper;
    private final SimulationMapper simulationMapper;

    public SimulationVO createSimulation(Long userId, CreateSimulationRequestDTO request){
        SimulationVO activeSimulation = simulationMapper.findActiveByUserId(userId);

        if(activeSimulation != null) return activeSimulation;

        FinancialSnapshotVO snapshot = financialSnapshotMapper.findLatestByUserId(userId);

        if(snapshot == null) throw ApplicationException.from(AnalysisErrorCode.SNAPSHOT_NOT_FOUND);

        if(!userId.equals(snapshot.getUserId())) throw ApplicationException.from(AnalysisErrorCode.SNAPSHOT_OWNER_MISMATCH);

        SimulationVO simulation = SimulationVO.createCurrentSimulation(
                userId,
                request,
                snapshot
            );

        simulationMapper.save(simulation);
        return simulation;
    }
}
