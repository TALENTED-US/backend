package com.talented.buttie.catalog.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PolicyRegionMapMapper {

    void insert(@Param("policyId") Long policyId, @Param("zipCd") String zipCd, @Param("regionName") String regionName);

    int deleteByPolicyId(@Param("policyId") Long policyId);
}
