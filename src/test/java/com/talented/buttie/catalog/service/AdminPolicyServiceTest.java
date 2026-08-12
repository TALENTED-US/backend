package com.talented.buttie.catalog.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;

import com.talented.buttie.catalog.domain.AmountParseConfidence;
import com.talented.buttie.catalog.domain.PolicyStatus;
import com.talented.buttie.catalog.domain.PolicyVO;
import com.talented.buttie.catalog.dto.request.AdminPolicyUpsertRequest;
import com.talented.buttie.catalog.dto.response.AdminPolicyResponse;
import com.talented.buttie.catalog.exception.CatalogErrorCode;
import com.talented.buttie.catalog.mapper.PolicyMapper;
import com.talented.buttie.catalog.mapper.PolicyRegionMapMapper;
import com.talented.buttie.common.exception.ApplicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminPolicyServiceTest {

    @Mock
    private PolicyMapper policyMapper;

    @Mock
    private PolicyRegionMapMapper policyRegionMapMapper;

    @InjectMocks
    private AdminPolicyService adminPolicyService;

    @Test
    @DisplayName("정책 삭제 시 POLICY_REGION_MAP을 먼저 지워야 FK 위반 없이 POLICY를 지울 수 있다.")
    void deletePolicyClearsRegionMapBeforeDeletingPolicy() {
        given(policyMapper.deleteById(1L)).willReturn(1);

        adminPolicyService.deletePolicy(1L);

        InOrder order = inOrder(policyRegionMapMapper, policyMapper);
        order.verify(policyRegionMapMapper).deleteByPolicyId(1L);
        order.verify(policyMapper).deleteById(1L);
    }

    @Test
    @DisplayName("수정 응답은 임시 객체가 아니라 재조회한 실제 저장값(externalSource, 신뢰도 포함)을 반환한다.")
    void updatePolicyReturnsPersistedMetadataNotTransientObject() {
        given(policyMapper.update(any())).willReturn(1);
        PolicyVO savedPolicy = PolicyVO.builder()
            .policyId(1L)
            .policyName("수정된 정책")
            .policyCategory("취업")
            .familyCount(1)
            .policyStatus(PolicyStatus.AVAILABLE)
            .externalSource("YOUTHCENTER")
            .externalPolicyId("plcy-1")
            .amountParseConfidence(AmountParseConfidence.HIGH)
            .build();
        given(policyMapper.findById(1L)).willReturn(savedPolicy);

        AdminPolicyUpsertRequest request = AdminPolicyUpsertRequest.builder()
            .policyName("수정된 정책")
            .policyCategory("취업")
            .policyStatus(PolicyStatus.AVAILABLE)
            .build();

        AdminPolicyResponse response = adminPolicyService.updatePolicy(1L, request);

        assertEquals("YOUTHCENTER", response.externalSource());
        assertEquals(AmountParseConfidence.HIGH, response.amountParseConfidence());
    }

    @Test
    @DisplayName("삭제 대상이 없으면 POLICY_NOT_FOUND 예외를 던진다.")
    void deletePolicyThrowsWhenNotFound() {
        given(policyMapper.deleteById(1L)).willReturn(0);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> adminPolicyService.deletePolicy(1L)
        );

        assertEquals(CatalogErrorCode.POLICY_NOT_FOUND, exception.getCode());
    }
}
