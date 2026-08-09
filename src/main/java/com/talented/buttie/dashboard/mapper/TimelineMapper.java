package com.talented.buttie.dashboard.mapper;

import com.talented.buttie.dashboard.domain.TimelineVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TimelineMapper {
    TimelineVO findTimelineByUserId(@Param("userId") Long userId);
}
