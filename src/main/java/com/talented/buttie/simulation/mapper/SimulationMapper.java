package com.talented.buttie.simulation.mapper;

import com.talented.buttie.simulation.domain.SimulationVO;
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
}
