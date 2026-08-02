package com.talented.buttie.simulation.mapper;

import com.talented.buttie.simulation.domain.MonthlyProjectionVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MonthlyProjectionMapper {
    List<MonthlyProjectionVO> findAllBySimulationId(@Param("simulationId") Long simulationId);
    int deleteAllBySimulationId(@Param("simulationId") Long simulationId);
    void saveAll(@Param("projections") List<MonthlyProjectionVO> projections);
}
