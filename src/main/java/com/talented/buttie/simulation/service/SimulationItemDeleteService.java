package com.talented.buttie.simulation.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.simulation.domain.MonthlyProjectionVO;
import com.talented.buttie.simulation.domain.SimulationItemVO;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.exception.SimulationErrorCode;
import com.talented.buttie.simulation.mapper.MonthlyProjectionMapper;
import com.talented.buttie.simulation.mapper.SimulationItemMapper;
import com.talented.buttie.simulation.mapper.SimulationMapper;
import com.talented.buttie.snapshot.domain.FinancialSnapshotVO;
import com.talented.buttie.snapshot.exception.AnalysisErrorCode;
import com.talented.buttie.snapshot.mapper.FinancialSnapshotMapper;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SimulationItemDeleteService {

    private final SimulationMapper simulationMapper;
    private final SimulationItemMapper simulationItemMapper;
    private final MonthlyProjectionMapper monthlyProjectionMapper;
    private final FinancialSnapshotMapper financialSnapshotMapper;
    private final SimulationItemCalculationService simulationItemCalculationService;

    @Transactional
    public void deleteItem(Long userId, Long itemId) {

        SimulationVO simulation = findUpdatableSimulation(userId);

        int deletedRows = simulationItemMapper.deleteByIdAndSimulationId(itemId, simulation.getSimulationId());

        if(deletedRows == 0) {
            throw ApplicationException.from(SimulationErrorCode.SIMULATION_ITEM_NOT_FOUND);
        }

        recalculateSimulation(simulation);
    }

    private void recalculateSimulation(SimulationVO simulation) {
        FinancialSnapshotVO snapshot = financialSnapshotMapper.findById(simulation.getSnapshotId());

        if (snapshot == null) {
            throw ApplicationException.from(AnalysisErrorCode.SNAPSHOT_NOT_FOUND);
        }

        List<SimulationItemVO> appliedItems = simulationItemMapper.findAllActiveBySimulationId(simulation.getSimulationId());

        List<MonthlyProjectionVO> recalculatedProjections = simulationItemCalculationService
            .recalculateProjections(simulation, snapshot, appliedItems);

        monthlyProjectionMapper.deleteAllBySimulationId(simulation.getSimulationId());
        monthlyProjectionMapper.saveAll(recalculatedProjections);

        MonthlyProjectionVO lastProjection = recalculatedProjections.get(recalculatedProjections.size() - 1);

        BigDecimal expectedPrepMonths = simulationItemCalculationService
            .calculateExpectedPrepMonths(recalculatedProjections, snapshot);

        simulationMapper.updateSummary(simulation.getSimulationId(), lastProjection.getClosingBalance(), expectedPrepMonths);
    }

    private SimulationVO findUpdatableSimulation(Long userId) {
        SimulationVO simulation = simulationMapper.findActiveByUserId(userId);

        if(simulation != null) {
            return simulation;
        }

        if (simulationMapper.findLatestConfirmedByUserId(userId) != null) {
            throw ApplicationException.from(SimulationErrorCode.CONFIRMED_SIMULATION_CANNOT_BE_UPDATED);
        }

        throw ApplicationException.from(SimulationErrorCode.NOT_CONFIRMED_SIMULATION_NOT_FOUND);
    }
}
