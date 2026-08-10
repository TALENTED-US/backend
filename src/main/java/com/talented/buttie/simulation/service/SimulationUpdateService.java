package com.talented.buttie.simulation.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.simulation.domain.MonthlyProjectionVO;
import com.talented.buttie.simulation.domain.SimulationItemVO;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.dto.request.UpdateSimulationPeriodRequest;
import com.talented.buttie.simulation.exception.SimulationErrorCode;
import com.talented.buttie.simulation.mapper.MonthlyProjectionMapper;
import com.talented.buttie.simulation.mapper.SimulationItemMapper;
import com.talented.buttie.simulation.mapper.SimulationMapper;
import com.talented.buttie.snapshot.domain.FinancialSnapshotVO;
import com.talented.buttie.snapshot.exception.AnalysisErrorCode;
import com.talented.buttie.snapshot.mapper.FinancialSnapshotMapper;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SimulationUpdateService {

    private final SimulationMapper simulationMapper;
    private final SimulationItemMapper simulationItemMapper;
    private final MonthlyProjectionMapper monthlyProjectionMapper;
    private final FinancialSnapshotMapper financialSnapshotMapper;
    private final SimulationItemCalculationService simulationItemCalculationService;

    // 미확정 시뮬레이션 수행 기간 수정
    @Transactional
    public void updateSimulationPeriod(
        Long userId,
        UpdateSimulationPeriodRequest request
    ) {
        validatePeriod(request.simulationStartDate(), request.simulationDueDate());

        SimulationVO simulation = findUpdatableSimulation(userId);
        FinancialSnapshotVO snapshot = financialSnapshotMapper.findById(simulation.getSnapshotId());

        if (snapshot == null) {
            throw ApplicationException.from(AnalysisErrorCode.SNAPSHOT_NOT_FOUND);
        }

        simulation.setSimulationStartDate(request.simulationStartDate());
        simulation.setSimulationDueDate(request.simulationDueDate());

        List<SimulationItemVO> appliedItems =
            simulationItemMapper.findAllActiveBySimulationId(simulation.getSimulationId());

        List<MonthlyProjectionVO> recalculatedProjections =
            simulationItemCalculationService.recalculateProjections(simulation, snapshot, appliedItems);

        int simulationEndAmount = recalculatedProjections
            .get(recalculatedProjections.size() - 1)
            .getClosingBalance();

        int updatedRows = simulationMapper.updateSimulationPeriod(
            simulation.getSimulationId(),
            request.simulationStartDate(),
            request.simulationDueDate(),
            simulationEndAmount
        );

        if (updatedRows == 0)
            throw ApplicationException.from(SimulationErrorCode.NOT_CONFIRMED_SIMULATION_NOT_FOUND);

        monthlyProjectionMapper.deleteAllBySimulationId(simulation.getSimulationId());
        monthlyProjectionMapper.saveAll(recalculatedProjections);

        simulationMapper.updateSummary(
            simulation.getSimulationId(),
            simulationEndAmount,
            simulationItemCalculationService.calculateExpectedPrepMonths(recalculatedProjections, snapshot)
        );
    }

    // 시뮬레이션 최종 확정
    @Transactional
    public void confirmSimulation(Long userId) {
        SimulationVO simulation = findUpdatableSimulation(userId);

        int confirmedRows = simulationMapper.confirmSimulation(simulation.getSimulationId());

        if(confirmedRows == 0) {
            throw ApplicationException.from(SimulationErrorCode.SIMULATION_CANNOT_BE_CONFIRMED);
        }
    }

    private SimulationVO findUpdatableSimulation(Long userId) {
        SimulationVO activeSimulation = simulationMapper.findActiveByUserId(userId);

        if (activeSimulation != null) {
            return activeSimulation;
        }

        if (simulationMapper.findLatestConfirmedByUserId(userId) != null) {
            throw ApplicationException.from(SimulationErrorCode.CONFIRMED_SIMULATION_CANNOT_BE_UPDATED);
        }

        throw ApplicationException.from(SimulationErrorCode.NOT_CONFIRMED_SIMULATION_NOT_FOUND);
    }

    private void validatePeriod(
        LocalDate simulationStartDate,
        LocalDate simulationDueDate
    ) {
        if (simulationStartDate == null
            || simulationDueDate == null
            || !simulationDueDate.isAfter(simulationStartDate)) {
            throw ApplicationException.from(
                SimulationErrorCode.INVALID_SIMULATION_PERIOD
            );
        }
    }

    // 확정 시뮬레이션 미확정으로 되돌리기
    public void revertSimulation(Long userId) {
        SimulationVO confirmedSimulation = simulationMapper.findLatestConfirmedByUserId(userId);

        if(confirmedSimulation == null) {
            throw ApplicationException.from(SimulationErrorCode.CONFIRMED_SIMULATION_NOT_FOUND);
        }

        if(simulationMapper.findActiveByUserId(userId) != null) {
            throw ApplicationException.from(SimulationErrorCode.ALREADY_NOT_CONFIRMED_SIMULATION_EXISTS);
        }

        int updateRows = simulationMapper.revertSimulation(confirmedSimulation.getSimulationId());

        if(updateRows == 0) {
            throw ApplicationException.from(SimulationErrorCode.CONFIRMED_SIMULATION_NOT_FOUND);
        }
    }
}
