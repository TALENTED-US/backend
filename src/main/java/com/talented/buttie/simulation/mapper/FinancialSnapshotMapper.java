package com.talented.buttie.simulation.mapper;

import com.talented.buttie.simulation.domain.FinancialSnapshotVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FinancialSnapshotMapper {

    FinancialSnapshotVO findById(@Param("snapshotId") Long snapshotId);

    FinancialSnapshotVO findLatestByUserId(@Param("userId") Long userId);

    void save(FinancialSnapshotVO financialSnapshot);
}
