package com.talented.buttie.simulation.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.simulation.domain.MonthlyProjectionVO;
import com.talented.buttie.simulation.domain.SimulationItemVO;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.dto.request.ApplySimulationItemRequest;
import com.talented.buttie.simulation.dto.request.CreateSimulationRequest;
import com.talented.buttie.simulation.dto.response.PreviewItemResponse;
import com.talented.buttie.simulation.exception.SimulationErrorCode;
import com.talented.buttie.simulation.mapper.MonthlyProjectionMapper;
import com.talented.buttie.simulation.mapper.SimulationItemMapper;
import com.talented.buttie.simulation.mapper.SimulationMapper;
import com.talented.buttie.snapshot.domain.FinancialSnapshotVO;
import com.talented.buttie.snapshot.exception.AnalysisErrorCode;
import com.talented.buttie.snapshot.mapper.FinancialSnapshotMapper;
import com.talented.buttie.snapshot.service.FinancialSnapshotCreateService;
import com.talented.buttie.user.mapper.EmploymentPreparationMapper;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SimulationCreateService {

    private final FinancialSnapshotCreateService financialSnapshotCreateService;
    private final FinancialSnapshotMapper financialSnapshotMapper;
    private final SimulationMapper simulationMapper;
    private final SimulationItemMapper simulationItemMapper;
    private final MonthlyProjectionMapper monthlyProjectionMapper;
    private final ProjectionEngine projectionEngine;
    private final EmploymentPreparationMapper employmentPreparationMapper;
    private final SimulationItemCalculationService simulationItemCalculationService;

    @Transactional
    public SimulationVO createSimulation(Long userId, CreateSimulationRequest request){
        SimulationVO activeSimulation = simulationMapper.findActiveByUserId(userId);

        if(activeSimulation != null) return activeSimulation;

        FinancialSnapshotVO snapshot = financialSnapshotCreateService.createSnapshot(userId);

        SimulationVO simulation = SimulationVO.createCurrentSimulation(userId, request, snapshot);
        simulationMapper.save(simulation);

        Integer livingThreshold = employmentPreparationMapper.getLivingThresholdByUserId(userId);

        List<MonthlyProjectionVO> monthlyProjections = projectionEngine.createInitialProjections(
            simulation.getSimulationId(), simulation.getSimulationStartDate(),
            simulation.getSimulationDueDate(), valueOf(snapshot.getLiquidAssets()),
            intValueOf(snapshot.getAvgMonthlyIncome()), intValueOf(snapshot.getAvgMonthlyExpense()), valueOf(livingThreshold)
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

    @Transactional(readOnly = true)
    public PreviewItemResponse previewItemResultSimulation(Long userId, ApplySimulationItemRequest request){
        SimulationVO simulation = simulationMapper.findActiveByUserId(userId);

        if(simulation == null){
            throw new ApplicationException(SimulationErrorCode.SIMULATION_NOT_FOUND);
        }

        simulationItemCalculationService.validateRequest(request, simulation);

        FinancialSnapshotVO snapshot = financialSnapshotMapper.findLatestByUserId(userId);

        if(snapshot == null) {
            throw ApplicationException.from(AnalysisErrorCode.SNAPSHOT_NOT_FOUND);
        }

        List<MonthlyProjectionVO> beforeProjections = findBeforeProjections(simulation, snapshot);
        SimulationItemVO previewItem = simulationItemCalculationService.createItem(
            simulation,
            request,
            simulationItemCalculationService.resolvePolicy(request)
        );

        List<SimulationItemVO> appliedItems = new java.util.ArrayList<>(
            simulationItemMapper.findAllActiveBySimulationId(simulation.getSimulationId())
        );
        appliedItems.add(previewItem);

        List<MonthlyProjectionVO> afterProjections =
            simulationItemCalculationService.recalculateProjections(simulation, snapshot, appliedItems);

        return simulationItemCalculationService.createPreviewResponse(
            previewItem,
            beforeProjections,
            afterProjections
        );
    }

    private List<MonthlyProjectionVO> findBeforeProjections(
        SimulationVO simulation,
        FinancialSnapshotVO snapshot
    ) {
        List<MonthlyProjectionVO> currentProjections =
            monthlyProjectionMapper.findAllBySimulationId(simulation.getSimulationId());

        if (currentProjections != null && !currentProjections.isEmpty()) {
            return currentProjections.stream()
                .sorted(Comparator.comparing(MonthlyProjectionVO::getProjectionMonth))
                .toList();
        }

        return simulationItemCalculationService.createBaselineProjections(simulation, snapshot);
    }
}
