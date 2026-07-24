package com.talented.buttie.simulation.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.dto.request.CreateSimulationRequest;
import com.talented.buttie.simulation.mapper.SimulationMapper;
import com.talented.buttie.snapshot.domain.FinancialSnapshotVO;
import com.talented.buttie.snapshot.exception.AnalysisErrorCode;
import com.talented.buttie.snapshot.mapper.FinancialSnapshotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SimulationPostService {

    private final FinancialSnapshotMapper financialSnapshotMapper;
    private final SimulationMapper simulationMapper;

    public SimulationVO createSimulation(Long userId, CreateSimulationRequest request){
        SimulationVO activeSimulation = simulationMapper.findActiveByUserId(userId);

        if(activeSimulation != null) return activeSimulation;

        FinancialSnapshotVO snapshot = financialSnapshotMapper.findLatestByUserId(userId);

        if(snapshot == null) throw ApplicationException.from(AnalysisErrorCode.SNAPSHOT_NOT_FOUND);

        if(!userId.equals(snapshot.getUserId())) throw ApplicationException.from(AnalysisErrorCode.SNAPSHOT_OWNER_MISMATCH);

        SimulationVO simulation = SimulationVO.builder()
            .userId(userId)
            .snapshotId(snapshot.getSnapshotId())
            .startDate(request.startDate())
            .endDate(request.endDate())
            .endingBalance(snapshot.getLiquidAssets()) // TODO: 예상 재정 계획 구현(ProjectionEngine) 후 결과로 교체
            .targetRate(snapshot.getTargetAchievementRate())
            .prepMonths(snapshot.getPrepPossibleMonths())
            .build();

        simulationMapper.save(simulation);
        return simulation;
    }
}
