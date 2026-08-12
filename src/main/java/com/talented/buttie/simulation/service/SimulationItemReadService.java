package com.talented.buttie.simulation.service;

import com.talented.buttie.catalog.domain.PolicyVO;
import com.talented.buttie.catalog.mapper.PolicyMapper;
import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.simulation.domain.MonthlyProjectionVO;
import com.talented.buttie.simulation.domain.SimulationItemCategory;
import com.talented.buttie.simulation.domain.SimulationItemVO;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.dto.response.SimulationItemReportResponse;
import com.talented.buttie.simulation.dto.response.SimulationItemResponse;
import com.talented.buttie.simulation.dto.response.SimulationItemsByCategoryResponse;
import com.talented.buttie.simulation.exception.SimulationErrorCode;
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
public class SimulationItemReadService {

    private final SimulationMapper simulationMapper;
    private final SimulationItemMapper simulationItemMapper;
    private final FinancialSnapshotMapper financialSnapshotMapper;
    private final SimulationItemCalculationService simulationItemCalculationService;
    private final PolicyMapper policyMapper;

    @Transactional(readOnly = true)
    public SimulationItemReportResponse getAppliedItemReport(Long userId) {
        SimulationVO simulation = simulationMapper.findActiveByUserId(userId);

        if (simulation == null) {
            throw ApplicationException.from(SimulationErrorCode.NOT_CONFIRMED_SIMULATION_NOT_FOUND);
        }

        FinancialSnapshotVO snapshot = financialSnapshotMapper.findLatestByUserId(userId);

        if (snapshot == null) {
            throw ApplicationException.from(AnalysisErrorCode.SNAPSHOT_NOT_FOUND);
        }

        List<SimulationItemVO> appliedItems =
            simulationItemMapper.findAllActiveBySimulationId(simulation.getSimulationId());

        List<MonthlyProjectionVO> beforeProjections =
            simulationItemCalculationService.createBaselineProjections(simulation, snapshot);

        List<MonthlyProjectionVO> afterProjections =
            simulationItemCalculationService.recalculateProjections(simulation, snapshot, appliedItems);

        return simulationItemCalculationService.createReportResponse(
            userId,
            snapshot,
            appliedItems,
            beforeProjections,
            afterProjections
        );
    }

    @Transactional(readOnly = true)
    public SimulationItemsByCategoryResponse findItemListByCategory(Long userId, SimulationItemCategory itemCategory) {
        SimulationVO simulation = simulationMapper.findActiveByUserId(userId);

        if (simulation == null) {
            throw ApplicationException.from(SimulationErrorCode.NOT_CONFIRMED_SIMULATION_NOT_FOUND);
        }

        List<SimulationItemResponse> items = simulationItemMapper
            .findAllByCategory(simulation.getSimulationId(), itemCategory)
            .stream()
            .map(item -> {
                PolicyVO policy = item.getPolicy() != null
                    ? item.getPolicy()
                    : (item.getPolicyId() != null ? policyMapper.findById(item.getPolicyId()) : null);
                return SimulationItemResponse.from(item, policy, simulation);
            })
            .toList();

        return new SimulationItemsByCategoryResponse(items);
    }

    @Transactional(readOnly = true)
    public List<SimulationItemResponse> findAllAppliedItems(SimulationVO simulation) {
        return simulationItemMapper.findAllActiveBySimulationId(simulation.getSimulationId())
            .stream()
            .map(item -> {
                PolicyVO policy = item.getPolicy() != null
                    ? item.getPolicy()
                    : (item.getPolicyId() != null ? policyMapper.findById(item.getPolicyId()) : null);
                return SimulationItemResponse.from(item, policy, simulation);
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal getCurrentPrepMonths(Long userId) {
        FinancialSnapshotVO snapshot = financialSnapshotMapper.findLatestByUserId(userId);

        if (snapshot == null) {
            throw ApplicationException.from(AnalysisErrorCode.SNAPSHOT_NOT_FOUND);
        }

        return snapshot.getCurrentPrepMonths();
    }
}
