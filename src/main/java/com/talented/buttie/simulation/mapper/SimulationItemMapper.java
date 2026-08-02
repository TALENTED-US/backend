package com.talented.buttie.simulation.mapper;

import com.talented.buttie.simulation.domain.SimulationItemVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SimulationItemMapper {
    List<SimulationItemVO> findAllActiveBySimulationId(@Param("simulationId") Long simulationId);
}
