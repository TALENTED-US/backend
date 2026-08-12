package com.talented.buttie.catalog.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.talented.buttie.catalog.external.youthcenter.YouthCenterPolicyClient;
import com.talented.buttie.catalog.external.youthcenter.YouthCenterPolicyItem;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyIngestionServiceTest {

    @Mock
    private YouthCenterPolicyClient youthCenterPolicyClient;

    @Mock
    private PolicyUpsertService policyUpsertService;

    @InjectMocks
    private PolicyIngestionService policyIngestionService;

    @Test
    @DisplayName("한 건이 실패해도 나머지 항목은 계속 처리한다 (항목별 독립 트랜잭션).")
    void continuesProcessingWhenOneItemFails() {
        YouthCenterPolicyItem failing = item("fail");
        YouthCenterPolicyItem ok = item("ok");
        given(youthCenterPolicyClient.fetchAll()).willReturn(List.of(failing, ok));
        willThrow(new RuntimeException("DB error")).given(policyUpsertService).upsertPolicy(failing);
        willDoNothing().given(policyUpsertService).upsertPolicy(ok);

        policyIngestionService.ingestYouthCenterPolicies();

        verify(policyUpsertService, times(1)).upsertPolicy(failing);
        verify(policyUpsertService, times(1)).upsertPolicy(ok);
    }

    @Test
    @DisplayName("온통청년 API 호출 자체가 실패하면 항목 처리 없이 조용히 종료한다.")
    void doesNothingWhenFetchAllFails() {
        given(youthCenterPolicyClient.fetchAll()).willThrow(new RuntimeException("network error"));

        policyIngestionService.ingestYouthCenterPolicies();

        verify(policyUpsertService, times(0)).upsertPolicy(any());
    }

    private YouthCenterPolicyItem item(String plcyNo) {
        return new YouthCenterPolicyItem(
            plcyNo, "테스트 정책", "일자리", "취업", "지원 내용",
            "18", "39", "Y", "11680", "0013010",
            "20260812 ~ 20260814", "        ", "        ",
            "https://example.com", "제출 서류"
        );
    }
}
