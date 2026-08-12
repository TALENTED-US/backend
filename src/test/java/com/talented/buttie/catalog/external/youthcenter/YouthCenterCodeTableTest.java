package com.talented.buttie.catalog.external.youthcenter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class YouthCenterCodeTableTest {

    @Test
    @DisplayName("실응답 기준 lclsfNm 값을 POLICY_CATEGORY로 정규화한다.")
    void normalizeCategory() {
        assertEquals("취업", YouthCenterCodeTable.normalizeCategory("일자리"));
        assertEquals("주거", YouthCenterCodeTable.normalizeCategory("주거"));
        assertEquals("교육", YouthCenterCodeTable.normalizeCategory("교육･직업훈련"));
        assertEquals("복지", YouthCenterCodeTable.normalizeCategory("금융･복지･문화"));
        assertEquals("참여", YouthCenterCodeTable.normalizeCategory("참여･기반"));
    }

    @Test
    @DisplayName("매핑에 없는 lclsfNm은 원본 값을 그대로 반환한다.")
    void normalizeCategoryUnknownValue() {
        assertEquals("알수없는분류", YouthCenterCodeTable.normalizeCategory("알수없는분류"));
    }

    @Test
    @DisplayName("jobCd를 EMPLOYMENT_PREP_STATUS로 매핑한다.")
    void resolveEmploymentPrepStatus() {
        assertEquals("재직자", YouthCenterCodeTable.resolveEmploymentPrepStatus("0013001"));
        assertEquals("기타", YouthCenterCodeTable.resolveEmploymentPrepStatus("0013009"));
    }

    @Test
    @DisplayName("제한없음(0013010)은 null로 매핑한다.")
    void resolveEmploymentPrepStatusUnlimited() {
        assertNull(YouthCenterCodeTable.resolveEmploymentPrepStatus("0013010"));
    }

    @Test
    @DisplayName("알 수 없는 jobCd는 null을 반환한다.")
    void resolveEmploymentPrepStatusUnknownCode() {
        assertNull(YouthCenterCodeTable.resolveEmploymentPrepStatus("9999999"));
    }

    @Test
    @DisplayName("jobCd가 콤마로 여러 개 오면 각각 매핑해서 콤마로 합친다.")
    void resolveEmploymentPrepStatusMultipleCodes() {
        assertEquals("영농종사자,기타", YouthCenterCodeTable.resolveEmploymentPrepStatus("0013008,0013009"));
    }
}
