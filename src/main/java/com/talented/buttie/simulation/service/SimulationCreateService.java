package com.talented.buttie.simulation.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.simulation.domain.MonthlyProjectionVO;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.dto.request.CreateSimulationRequestDTO;
import com.talented.buttie.simulation.mapper.MonthlyProjectionMapper;
import com.talented.buttie.simulation.mapper.SimulationMapper;
import com.talented.buttie.snapshot.domain.FinancialSnapshotVO;
import com.talented.buttie.snapshot.exception.AnalysisErrorCode;
import com.talented.buttie.snapshot.mapper.FinancialSnapshotMapper;
import com.talented.buttie.user.mapper.EmploymentPreparationMapper;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SimulationCreateService {

    private final FinancialSnapshotMapper financialSnapshotMapper;
    private final SimulationMapper simulationMapper;
    private final MonthlyProjectionMapper monthlyProjectionMapper;
    private final ProjectionEngine projectionEngine;
    private final EmploymentPreparationMapper employmentPreparationMapper;

    @Transactional
    public SimulationVO createSimulation(Long userId, CreateSimulationRequestDTO request){
        SimulationVO activeSimulation = simulationMapper.findActiveByUserId(userId);

        if(activeSimulation != null) return activeSimulation;

        FinancialSnapshotVO snapshot = financialSnapshotMapper.findLatestByUserId(userId);

        if(snapshot == null) throw ApplicationException.from(AnalysisErrorCode.SNAPSHOT_NOT_FOUND);

        if(!userId.equals(snapshot.getUserId())) throw ApplicationException.from(AnalysisErrorCode.SNAPSHOT_OWNER_MISMATCH);

        SimulationVO simulation = SimulationVO.createCurrentSimulation(userId, request, snapshot);
        simulationMapper.save(simulation);

        Integer livingThreshold = employmentPreparationMapper.getLivingThresholdByUserId(userId);

        List<MonthlyProjectionVO> monthlyProjections = projectionEngine.createInitialProjections(
            simulation.getSimulationId(), simulation.getSimulationStartDate(), simulation.getSimulationDueDate(),
            valueOf(snapshot.getLiquidAssets()), intValueOf(snapshot.getAvgMonthlyIncome()), intValueOf(snapshot.getAvgMonthlyExpense()), valueOf(livingThreshold)
        );
        monthlyProjectionMapper.saveAll(monthlyProjections);
        simulation.setMonthlyProjections(monthlyProjections);

        return simulation;
    }

    private int valueOf(Integer value){
        return value == null ? 0 : value;
    }

    private int intValueOf(BigDecimal value) {
        return value == null ? 0 : value.intValue();
    }
}
