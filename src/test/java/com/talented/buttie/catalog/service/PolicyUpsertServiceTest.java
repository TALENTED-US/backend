package com.talented.buttie.catalog.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.talented.buttie.catalog.domain.PolicyStatus;
import com.talented.buttie.catalog.domain.PolicyVO;
import com.talented.buttie.catalog.external.PolicySupportAmountParser;
import com.talented.buttie.catalog.external.PolicySupportAmountParser.ParseResult;
import com.talented.buttie.catalog.external.vworld.RegionCodeCache;
import com.talented.buttie.catalog.external.youthcenter.YouthCenterPolicyItem;
import com.talented.buttie.catalog.mapper.PolicyMapper;
import com.talented.buttie.catalog.mapper.PolicyRegionMapMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyUpsertServiceTest {

    @Mock(lenient = true)
    private RegionCodeCache regionCodeCache;

    @Mock
    private PolicySupportAmountParser amountParser;

    @Mock
    private PolicyMapper policyMapper;

    @Mock
    private PolicyRegionMapMapper policyRegionMapMapper;

    @InjectMocks
    private PolicyUpsertService policyUpsertService;

    @BeforeEach
    void setUp() {
        given(amountParser.parse(any())).willReturn(new ParseResult(100_000, 1, "HIGH"));
        willAnswer(invocation -> {
            PolicyVO policy = invocation.getArgument(0);
            policy.setPolicyId(100L);
            return null;
        }).given(policyMapper).upsert(any());
        given(regionCodeCache.resolve(anyString())).willReturn("서울특별시 강남구");
    }

    @Test
    @DisplayName("zipCd가 여러 개면 각각 지역 매핑을 저장한다.")
    void upsertPolicyWithMultipleZipCd() {
        YouthCenterPolicyItem item = item("11680,26680", "20260812 ~ 20260814");

        policyUpsertService.upsertPolicy(item);

        verify(policyRegionMapMapper).deleteByPolicyId(100L);
        verify(policyRegionMapMapper).insert(eq(100L), eq("11680"), anyString());
        verify(policyRegionMapMapper).insert(eq(100L), eq("26680"), anyString());
        verify(policyRegionMapMapper, times(2)).insert(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("zipCd가 1개면 지역 매핑을 1건만 저장한다.")
    void upsertPolicyWithSingleZipCd() {
        YouthCenterPolicyItem item = item("11680", "20260812 ~ 20260814");

        policyUpsertService.upsertPolicy(item);

        verify(policyRegionMapMapper).insert(eq(100L), eq("11680"), anyString());
        verify(policyRegionMapMapper, times(1)).insert(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("zipCd가 없으면 기존 매핑만 지우고 새로 저장하지 않는다.")
    void upsertPolicyWithNoZipCd() {
        YouthCenterPolicyItem item = item("", "20260812 ~ 20260814");

        policyUpsertService.upsertPolicy(item);

        verify(policyRegionMapMapper).deleteByPolicyId(100L);
        verify(policyRegionMapMapper, never()).insert(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("캐시에 없는 zipCd는 POLICY_REGION을 전국으로 뭉개지 않고 zipCd 그대로 표시한다.")
    void upsertPolicyWithUnresolvedZipCdDoesNotFallBackToNationwide() {
        given(regionCodeCache.resolve("99999")).willReturn(null);
        YouthCenterPolicyItem item = item("99999", "20260812 ~ 20260814");

        policyUpsertService.upsertPolicy(item);

        verify(policyMapper).upsert(argThat(policy -> "99999".equals(policy.getPolicyRegion())));
        verify(policyRegionMapMapper).insert(100L, "99999", null);
    }

    @Test
    @DisplayName("aplyYmd가 빈 값(상시모집)이면 마감일 없이 저장한다.")
    void upsertPolicyWithBlankAplyYmd() {
        YouthCenterPolicyItem item = item("11680", "");

        policyUpsertService.upsertPolicy(item);

        verify(policyMapper).upsert(argThat(policy -> policy.getDueDate() == null));
    }

    @Test
    @DisplayName("지원금액 신뢰도가 HIGH가 아니면 마감일과 무관하게 CLOSED로 저장해 공개/시뮬레이션 대상에서 제외한다.")
    void lowConfidencePolicyIsClosedRegardlessOfDueDate() {
        given(amountParser.parse(any())).willReturn(new ParseResult(null, null, "MANUAL"));
        YouthCenterPolicyItem item = item("11680", "20260812 ~ 20990101");

        policyUpsertService.upsertPolicy(item);

        verify(policyMapper).upsert(argThat(policy -> policy.getPolicyStatus() == PolicyStatus.CLOSED));
    }

    @Test
    @DisplayName("지원금액 신뢰도가 HIGH이고 마감일이 미래면 AVAILABLE로 저장한다.")
    void highConfidencePolicyIsAvailableWhenNotExpired() {
        YouthCenterPolicyItem item = item("11680", "20260812 ~ 20990101");

        policyUpsertService.upsertPolicy(item);

        verify(policyMapper).upsert(argThat(policy -> policy.getPolicyStatus() == PolicyStatus.AVAILABLE));
    }

    private YouthCenterPolicyItem item(String zipCd, String aplyYmd) {
        return new YouthCenterPolicyItem(
            "20260806005400213323",
            "테스트 정책",
            "일자리",
            "취업",
            "지원 내용",
            "18",
            "39",
            "Y",
            zipCd,
            "0013010",
            aplyYmd,
            "        ",
            "        ",
            "https://example.com",
            "제출 서류"
        );
    }
}
