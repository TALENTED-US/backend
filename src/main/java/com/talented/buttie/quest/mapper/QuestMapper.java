package com.talented.buttie.quest.mapper;

import com.talented.buttie.quest.domain.QuestVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface QuestMapper {

    List<QuestVO> findAllByUserId(@Param("userId") Long userId);

    List<QuestVO> findAllBySimulationId(@Param("simulationId") Long simulationId);
}
