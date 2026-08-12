package com.talented.buttie.catalog.external;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.talented.buttie.catalog.external.PolicySupportAmountParser.ParseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PolicySupportAmountParserTest {

    private final PolicySupportAmountParser parser = new PolicySupportAmountParser();

    @Test
    @DisplayName("월 지원 키워드 + 개월수가 함께 있으면 HIGH로 확정한다.")
    void monthlyAmountWithMonthCount() {
        ParseResult result = parser.parse("월 최대 20만원, 최대 24개월 지원");

        assertEquals(200_000, result.amount());
        assertEquals(24, result.supportMonthCount());
        assertEquals("HIGH", result.confidence());
    }

    @Test
    @DisplayName("금액 후보가 없으면 MANUAL이다.")
    void noAmountCandidate() {
        ParseResult result = parser.parse("구직활동에 필요한 어학·자격증 시험 응시료 지원");

        assertNull(result.amount());
        assertEquals("MANUAL", result.confidence());
    }

    @Test
    @DisplayName("서로 다른 금액 후보가 여러 개면 MANUAL이다.")
    void multipleDistinctCandidates() {
        ParseResult result = parser.parse("1등 100만원, 2등 50만원, 3등 30만원");

        assertNull(result.amount());
        assertEquals("MANUAL", result.confidence());
    }

    @Test
    @DisplayName("시급/생활임금 표현은 지원금액 후보에서 제외되어 MANUAL이다.")
    void hourlyWageExcluded() {
        ParseResult result = parser.parse("시간당 13,303원(생활임금)");

        assertNull(result.amount());
        assertEquals("MANUAL", result.confidence());
    }

    @Test
    @DisplayName("월 지원 키워드는 있지만 개월수를 특정할 수 없으면 LOW다.")
    void monthlyAmountWithoutMonthCount() {
        ParseResult result = parser.parse("매월 15만원씩 지원");

        assertEquals(150_000, result.amount());
        assertNull(result.supportMonthCount());
        assertEquals("LOW", result.confidence());
    }

    @Test
    @DisplayName("천 단위 구분자가 있는 만원 금액도 온전히 파싱한다.")
    void commaSeparatedManwonAmount() {
        assertEquals(10_000_000, parser.parse("최대 1,000만원 지원").amount());
        assertEquals(25_000_000, parser.parse("최대 2,500만원 지원").amount());
    }

    @Test
    @DisplayName("금액 표현이 없으면 MANUAL이다.")
    void noAmountMentioned() {
        ParseResult result = parser.parse("청년 1인 1호실 독립공간 무료 제공");

        assertNull(result.amount());
        assertEquals("MANUAL", result.confidence());
    }
}
