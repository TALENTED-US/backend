package com.talented.buttie.catalog.mapper;

import com.talented.buttie.catalog.domain.PolicyVO;
import com.talented.buttie.catalog.dto.request.PolicySearchRequest;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PolicyMapper {

    // =========================================================
    // 1. 사용자 조회 및 페이징/조건 검색
    // =========================================================
    PolicyVO findById(@Param("policyId") Long policyId);

    List<PolicyVO> findAll();

    List<PolicyVO> findAllPaginated(
        @Param("offset") int offset,
        @Param("pageSize") int pageSize
    );

    long countAll();

    List<PolicyVO> searchPolicies(
        @Param("request") PolicySearchRequest request,
        @Param("offset") int offset,
        @Param("pageSize") int pageSize
    );

    long countSearchPolicies(@Param("request") PolicySearchRequest request);

    // =========================================================
    // 2. 어드민 관리자 및 Batch 수집 CUD (Insert/Update/Delete/Upsert)
    // =========================================================
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
     * EXTERNAL_SOURCE + EXTERNAL_POLICY_ID 기준 upsert.
     * 수행 완료 시 policy.policyId에 자동 생성된 PK가 채워진다.
     */
    void upsert(PolicyVO policy);
}
