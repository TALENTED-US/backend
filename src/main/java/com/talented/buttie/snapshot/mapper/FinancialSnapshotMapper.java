package com.talented.buttie.snapshot.mapper;

import com.talented.buttie.snapshot.dto.result.SimulationSnapshotResultDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FinancialSnapshotMapper {
    SimulationSnapshotResultDTO findLatestByUserId(@Param("userId") Long userId);
}
