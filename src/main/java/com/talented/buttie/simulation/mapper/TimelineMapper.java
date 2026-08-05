package com.talented.buttie.simulation.mapper;

import com.talented.buttie.simulation.domain.TimelineVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TimelineMapper {
    TimelineVO findTimelineByUserId(@Param("userId") Long userId);
}
