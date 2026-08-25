package com.talented.buttie.simulation.mapper;

import com.talented.buttie.simulation.domain.SimulationItemCategory;
import com.talented.buttie.simulation.domain.SimulationItemVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SimulationItemMapper {
    List<SimulationItemVO> findAllActiveBySimulationId(@Param("simulationId") Long simulationId);
    void save(SimulationItemVO item);
    List<SimulationItemVO> findAllByCategory(
        @Param("simulationId") Long simulationId,
        @Param("itemCategory") SimulationItemCategory itemCategory
    );
    SimulationItemVO findById(@Param("simulationItemId") Long simulationItemId);
    SimulationItemVO findActiveByIdAndSimulationId(@Param("itemId") Long itemId, @Param("simulationId") Long simulationId);
    int update(SimulationItemVO item);
    int deleteByIdAndSimulationId(@Param("itemId") Long itemId, @Param("simulationId") Long simulationId);
}
