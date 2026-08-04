package com.talented.buttie.simulation.service;

import com.talented.buttie.catalog.domain.PolicyVO;
import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.simulation.domain.MonthlyProjectionVO;
import com.talented.buttie.simulation.domain.SimulationItemVO;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.dto.request.ApplySimulationItemRequest;
import com.talented.buttie.simulation.dto.response.ApplySimulationItemResponse;
import com.talented.buttie.simulation.exception.SimulationErrorCode;
import com.talented.buttie.simulation.mapper.MonthlyProjectionMapper;
import com.talented.buttie.simulation.mapper.SimulationItemMapper;
import com.talented.buttie.simulation.mapper.SimulationMapper;
import com.talented.buttie.snapshot.domain.FinancialSnapshotVO;
import com.talented.buttie.snapshot.exception.AnalysisErrorCode;
import com.talented.buttie.snapshot.mapper.FinancialSnapshotMapper;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SimulationItemCreateService {

    private final SimulationMapper simulationMapper;
    private final SimulationItemMapper simulationItemMapper;
    private final MonthlyProjectionMapper monthlyProjectionMapper;
    private final FinancialSnapshotMapper financialSnapshotMapper;
    private final SimulationItemCalculationService simulationItemCalculationService;

    @Transactional
    public ApplySimulationItemResponse applyItem(Long userId, ApplySimulationItemRequest request) {
        SimulationVO simulation = simulationMapper.findActiveByUserId(userId);

        if (simulation == null) {
            throw ApplicationException.from(SimulationErrorCode.SIMULATION_NOT_FOUND);
        }

        if (simulation.getConfirmedAt() != null) {
            throw ApplicationException.from(SimulationErrorCode.CONFIRMED_SIMULATION_CANNOT_BE_UPDATED);
        }

        simulationItemCalculationService.validateRequest(request, simulation);

        FinancialSnapshotVO snapshot = financialSnapshotMapper.findLatestByUserId(userId);

        if (snapshot == null) {
            throw ApplicationException.from(AnalysisErrorCode.SNAPSHOT_NOT_FOUND);
        }

        PolicyVO policy = simulationItemCalculationService.resolvePolicy(request);
        SimulationItemVO item = simulationItemCalculationService.createItem(simulation, request, policy);
        simulationItemMapper.save(item);

        List<MonthlyProjectionVO> beforeProjections = findBeforeProjections(simulation, snapshot);
        List<SimulationItemVO> appliedItems = simulationItemMapper.findAllActiveBySimulationId(simulation.getSimulationId());
        List<MonthlyProjectionVO> recalculatedProjections =
            simulationItemCalculationService.recalculateProjections(simulation, snapshot, appliedItems);

        monthlyProjectionMapper.deleteAllBySimulationId(simulation.getSimulationId());
        monthlyProjectionMapper.saveAll(recalculatedProjections);

        MonthlyProjectionVO lastProjection = recalculatedProjections.get(recalculatedProjections.size() - 1);
        BigDecimal prepMonths = simulationItemCalculationService.calculatePrepMonths(recalculatedProjections);

        simulationMapper.updateSummary(simulation.getSimulationId(), lastProjection.getClosingBalance(), prepMonths);

        return new ApplySimulationItemResponse(
            item.getSimulationItemId(),
            simulationItemCalculationService.createPreviewResponse(item, beforeProjections, recalculatedProjections)
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
