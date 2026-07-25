package com.talented.buttie.simulation.mapper;

import com.talented.buttie.simulation.domain.SimulationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SimulationMapper {
    SimulationVO findActiveByUserId(@Param("userId") Long userId);
    void save(SimulationVO simulationVO);
}
