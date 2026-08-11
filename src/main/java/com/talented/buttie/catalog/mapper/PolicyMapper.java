package com.talented.buttie.catalog.mapper;

import com.talented.buttie.catalog.domain.PolicyVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PolicyMapper {

    PolicyVO findById(@Param("policyId") Long policyId);

    List<PolicyVO> search(
        @Param("externalSource") String externalSource,
        @Param("amountParseConfidence") String amountParseConfidence,
        @Param("offset") int offset,
        @Param("size") int size
    );

    int countBySearch(
        @Param("externalSource") String externalSource,
        @Param("amountParseConfidence") String amountParseConfidence
    );

    void insert(PolicyVO policy);

    int update(PolicyVO policy);

    int deleteById(@Param("policyId") Long policyId);

    /**
     * EXTERNAL_SOURCE + EXTERNAL_POLICY_ID 기준 upsert. policy.policyId에 PK가 채워진다.
     */
    void upsert(PolicyVO policy);
}
