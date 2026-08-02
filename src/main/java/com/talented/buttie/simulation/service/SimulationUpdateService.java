package com.talented.buttie.simulation.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.simulation.domain.MonthlyProjectionVO;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.dto.request.UpdateSimulationPeriodRequestDTO;
import com.talented.buttie.simulation.exception.SimulationErrorCode;
import com.talented.buttie.simulation.mapper.MonthlyProjectionMapper;
import com.talented.buttie.simulation.mapper.SimulationMapper;
import com.talented.buttie.user.mapper.EmploymentPreparationMapper;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SimulationUpdateService {

    private final SimulationMapper simulationMapper;
    private final MonthlyProjectionMapper monthlyProjectionMapper;
    private final ProjectionEngine projectionEngine;
    private final EmploymentPreparationMapper employmentPreparationMapper;

    @Transactional
    public void updateSimulationPeriod(
        Long userId,
        UpdateSimulationPeriodRequestDTO request
    ) {
        validatePeriod(request.simulationStartDate(), request.simulationDueDate());

        SimulationVO simulation = findUpdatableSimulation(userId);
        List<MonthlyProjectionVO> existingProjections =
            monthlyProjectionMapper.findAllBySimulationId(simulation.getSimulationId());

        validateProjectionExists(existingProjections);

        Integer livingThreshold = employmentPreparationMapper.getLivingThresholdByUserId(userId);

        List<MonthlyProjectionVO> recalculatedProjections =
            projectionEngine.recalculateProjections(
                simulation.getSimulationId(), request.simulationStartDate(), request.simulationDueDate(),
                existingProjections, valueOf(livingThreshold)
            );

        int simulationEndAmount = recalculatedProjections
            .get(recalculatedProjections.size() - 1)
            .getClosingBalance();

        int updatedRows = simulationMapper.updateSimulationPeriod(
            simulation.getSimulationId(),
            request.simulationStartDate(),
            request.simulationDueDate(),
            simulationEndAmount
        );

        if (updatedRows == 0) {
            throw ApplicationException.from(SimulationErrorCode.SIMULATION_NOT_FOUND);
        }

        monthlyProjectionMapper.deleteAllBySimulationId(simulation.getSimulationId());
        monthlyProjectionMapper.saveAll(recalculatedProjections);
    }

    private SimulationVO findUpdatableSimulation(Long userId) {
        SimulationVO activeSimulation = simulationMapper.findActiveByUserId(userId);
        if (activeSimulation != null) {
            return activeSimulation;
        }

        SimulationVO confirmedSimulation =
            simulationMapper.findLatestConfirmedByUserId(userId);
        if (confirmedSimulation != null) {
            throw ApplicationException.from(
                SimulationErrorCode.CONFIRMED_SIMULATION_CANNOT_BE_UPDATED
            );
        }

        throw ApplicationException.from(SimulationErrorCode.SIMULATION_NOT_FOUND);
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

    private void validateProjectionExists(
        List<MonthlyProjectionVO> projections
    ) {
        if (projections == null || projections.isEmpty()) {
            throw ApplicationException.from(
                SimulationErrorCode.SIMULATION_PROJECTION_NOT_FOUND
            );
        }
    }

    private int valueOf(Integer value){
        return value == null ? 0 : value;
    }
}
