package com.talented.buttie.simulation.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.simulation.domain.MonthlyProjectionVO;
import com.talented.buttie.simulation.domain.SimulationItemCategory;
import com.talented.buttie.simulation.domain.SimulationItemVO;
import com.talented.buttie.simulation.domain.SimulationRecurrenceType;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.dto.request.UpdateSimulationItemRequest;
import com.talented.buttie.simulation.dto.response.SimulationItemResponse;
import com.talented.buttie.simulation.exception.SimulationErrorCode;
import com.talented.buttie.simulation.mapper.MonthlyProjectionMapper;
import com.talented.buttie.simulation.mapper.SimulationItemMapper;
import com.talented.buttie.simulation.mapper.SimulationMapper;
import com.talented.buttie.snapshot.domain.FinancialSnapshotVO;
import com.talented.buttie.snapshot.exception.AnalysisErrorCode;
import com.talented.buttie.snapshot.mapper.FinancialSnapshotMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SimulationItemUpdateService {

    private final SimulationMapper simulationMapper;
    private final SimulationItemMapper simulationItemMapper;
    private final MonthlyProjectionMapper monthlyProjectionMapper;
    private final FinancialSnapshotMapper financialSnapshotMapper;
    private final SimulationItemCalculationService simulationItemCalculationService;

    // 미확정 시뮬레이셔 항목 조건 수정
    @Transactional
    public SimulationItemResponse updateItem(Long userId, Long itemId, UpdateSimulationItemRequest request) {
        SimulationVO simulation = findUpdatableSimulation(userId);

        SimulationItemVO item = simulationItemMapper.findActiveByIdAndSimulationId(itemId, simulation.getSimulationId());

        if (item == null) {
            throw ApplicationException.from(SimulationErrorCode.SIMULATION_ITEM_NOT_FOUND);
        }

        if (item.getSimulationItemCategory() == SimulationItemCategory.POLICY) {
            throw ApplicationException.from(SimulationErrorCode.POLICY_ITEM_CANNOT_BE_UPDATED);
        }

        validateRequest(request);
        validateItemName(item, request);

        LocalDate applyEndDate = resolveApplyEndDate(request);

        validateApplyPeriod(simulation, request.applyStartDate(), applyEndDate);

        overwriteItem(item, request, applyEndDate);

        int updatedRows = simulationItemMapper.update(item);

        if (updatedRows == 0) {
            throw ApplicationException.from(SimulationErrorCode.SIMULATION_ITEM_NOT_FOUND);
        }

        recalculateSimulation(simulation);

        // 정책 항목은 수정 불가로 PolicyVO 조회가 필요하지 않다.
        return SimulationItemResponse.from(item, null, simulation);
    }

    private SimulationVO findUpdatableSimulation(Long userId) {
        SimulationVO simulation = simulationMapper.findActiveByUserId(userId);

        if (simulation != null) {
            return simulation;
        }

        if (simulationMapper.findLatestConfirmedByUserId(userId) != null) {
            throw ApplicationException.from(SimulationErrorCode.CONFIRMED_SIMULATION_CANNOT_BE_UPDATED);
        }

        throw ApplicationException.from(SimulationErrorCode.NOT_CONFIRMED_SIMULATION_NOT_FOUND);
    }

    private void validateRequest(UpdateSimulationItemRequest request) {
        if (request.amount() == null
            || request.applyStartDate() == null
            || request.recurrenceType() == null) {
            throw ApplicationException.from(SimulationErrorCode.INVALID_SIMULATION_ITEM);
        }
    }

    private void validateItemName(SimulationItemVO item, UpdateSimulationItemRequest request) {
        if (item.getSimulationItemCategory() == SimulationItemCategory.INCOME) {
            if (request.itemName() == null || request.itemName().isBlank()) {
                throw ApplicationException.from(SimulationErrorCode.INVALID_INCOME_ITEM_NAME);
            }
        }

        if (item.getSimulationItemCategory() == SimulationItemCategory.EXPENSE) {
            if (request.itemName() != null) {
                throw ApplicationException.from(SimulationErrorCode.ITEM_NAME_NOT_ALLOWED_FOR_EXPENSE);
            }
        }
    }

    private LocalDate resolveApplyEndDate(UpdateSimulationItemRequest request) {
        if (request.recurrenceType() == SimulationRecurrenceType.MONTHLY) {
            if (request.applyEndDate() == null) {
                throw ApplicationException.from(SimulationErrorCode.INVALID_SIMULATION_ITEM_APPLY_PERIOD);
            }
            return request.applyEndDate();
        }

        return request.applyStartDate();
    }

    private void validateApplyPeriod(SimulationVO simulation, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw ApplicationException.from(SimulationErrorCode.INVALID_SIMULATION_ITEM_APPLY_PERIOD);
        }

        if (startDate.isAfter(simulation.getSimulationDueDate())
            || endDate.isBefore(simulation.getSimulationStartDate())) {
            throw ApplicationException.from(SimulationErrorCode.INVALID_SIMULATION_ITEM_APPLY_PERIOD);
        }
    }

    private void overwriteItem(SimulationItemVO item, UpdateSimulationItemRequest request, LocalDate applyEndDate) {
        item.setSimulationItemName(request.itemName());
        item.setSimulationItemApplyAmount(request.amount());
        item.setApplyStartDate(request.applyStartDate());
        item.setApplyEndDate(applyEndDate);
        item.setRecurrenceType(request.recurrenceType());
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
}
