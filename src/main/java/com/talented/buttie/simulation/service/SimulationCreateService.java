package com.talented.buttie.simulation.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.dto.request.CreateSimulationRequestDTO;
import com.talented.buttie.simulation.mapper.SimulationMapper;
import com.talented.buttie.snapshot.dto.result.SimulationSnapshotResultDTO;
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

        SimulationSnapshotResultDTO snapshot = financialSnapshotMapper.findLatestByUserId(userId);

        if(snapshot == null) throw ApplicationException.from(AnalysisErrorCode.SNAPSHOT_NOT_FOUND);

        if(!userId.equals(snapshot.userId())) throw ApplicationException.from(AnalysisErrorCode.SNAPSHOT_OWNER_MISMATCH);

        SimulationVO simulation = SimulationVO.createCurrentSimulation(
                userId,
                snapshot.snapshotId(),
                request.startDate(),
                request.endDate(),
                snapshot.liquidAssets(), // TODO: 예상 재정 계획 구현(ProjectionEngine) 후 결과로 교체
                snapshot.targetAchievementRate(),
                snapshot.prepPossibleMonths()
            );

        simulationMapper.save(simulation);
        return simulation;
    }
}
