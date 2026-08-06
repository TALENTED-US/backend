package com.talented.buttie.snapshot.mapper;

import com.talented.buttie.snapshot.domain.FinancialSnapshotVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FinancialSnapshotMapper {
    FinancialSnapshotVO findById(@Param("snapshotId") Long snapshotId);
    FinancialSnapshotVO findLatestByUserId(@Param("userId") Long userId);
    void save(FinancialSnapshotVO financialSnapshot);
}
