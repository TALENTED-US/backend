package com.talented.buttie.catalog.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;

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
