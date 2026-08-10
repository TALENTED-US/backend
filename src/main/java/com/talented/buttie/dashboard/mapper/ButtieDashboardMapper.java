package com.talented.buttie.dashboard.mapper;

import com.talented.buttie.dashboard.domain.ButtieDashboardVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ButtieDashboardMapper {

    ButtieDashboardVO selectButtieDashboard(@Param("userId") Long userId);
}
