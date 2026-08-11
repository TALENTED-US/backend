package com.talented.buttie.catalog.mapper;

import com.talented.buttie.catalog.domain.PolicyVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PolicyMapper {
    PolicyVO findById(@Param("policyId") Long policyId);

    List<PolicyVO> findAll();
}
