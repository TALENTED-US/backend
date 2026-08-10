package com.talented.buttie.simulation.mapper;

import com.talented.buttie.simulation.domain.SimulationVO;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SimulationMapper {
    SimulationVO findActiveByUserId(@Param("userId") Long userId);
    SimulationVO findLatestConfirmedByUserId(@Param("userId") Long userId);
    void save(SimulationVO simulationVO);
    int updateSimulationPeriod(
        @Param("simulationId") Long simulationId,
        @Param("simulationStartDate") LocalDate simulationStartDate,
        @Param("simulationDueDate") LocalDate simulationDueDate,
        @Param("simulationEndAmount") Integer simulationEndAmount
    );
    void updateSummary(
        @Param("simulationId") Long simulationId,
        @Param("simulationEndAmount") Integer simulationEndAmount,
        @Param("expectPrepMonths") BigDecimal expectPrepMonths
    );
    int confirmSimulation(@Param("simulationId") Long simulationId);

    int deleteById(@Param("simulationId") Long simulationId);
}
