package com.talented.buttie.catalog.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.talented.buttie.catalog.domain.PolicyVO;
import com.talented.buttie.catalog.external.PolicySupportAmountParser;
import com.talented.buttie.catalog.external.PolicySupportAmountParser.ParseResult;
import com.talented.buttie.catalog.external.vworld.RegionCodeCache;
import com.talented.buttie.catalog.external.youthcenter.YouthCenterPolicyClient;
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
class PolicyIngestionServiceTest {

    @Mock
    private YouthCenterPolicyClient youthCenterPolicyClient;

    @Mock(lenient = true)
    private RegionCodeCache regionCodeCache;

    @Mock
    private PolicySupportAmountParser amountParser;

    @Mock
    private PolicyMapper policyMapper;

    @Mock
    private PolicyRegionMapMapper policyRegionMapMapper;

    @InjectMocks
    private PolicyIngestionService policyIngestionService;

    @BeforeEach
    void setUp() {
        given(amountParser.parse(any())).willReturn(new ParseResult(null, null, "MANUAL"));
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

        policyIngestionService.upsertPolicy(item);

        verify(policyRegionMapMapper).deleteByPolicyId(100L);
        verify(policyRegionMapMapper).insert(eq(100L), eq("11680"), anyString());
        verify(policyRegionMapMapper).insert(eq(100L), eq("26680"), anyString());
        verify(policyRegionMapMapper, times(2)).insert(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("zipCd가 1개면 지역 매핑을 1건만 저장한다.")
    void upsertPolicyWithSingleZipCd() {
        YouthCenterPolicyItem item = item("11680", "20260812 ~ 20260814");

        policyIngestionService.upsertPolicy(item);

        verify(policyRegionMapMapper).insert(eq(100L), eq("11680"), anyString());
        verify(policyRegionMapMapper, times(1)).insert(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("zipCd가 없으면 기존 매핑만 지우고 새로 저장하지 않는다.")
    void upsertPolicyWithNoZipCd() {
        YouthCenterPolicyItem item = item("", "20260812 ~ 20260814");

        policyIngestionService.upsertPolicy(item);

        verify(policyRegionMapMapper).deleteByPolicyId(100L);
        verify(policyRegionMapMapper, never()).insert(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("aplyYmd가 빈 값(상시모집)이면 마감일 없이 AVAILABLE로 저장한다.")
    void upsertPolicyWithBlankAplyYmd() {
        YouthCenterPolicyItem item = item("11680", "");

        policyIngestionService.upsertPolicy(item);

        verify(policyMapper).upsert(argThatDueDateIsNull());
    }

    private PolicyVO argThatDueDateIsNull() {
        return org.mockito.ArgumentMatchers.argThat(policy -> policy.getDueDate() == null);
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
