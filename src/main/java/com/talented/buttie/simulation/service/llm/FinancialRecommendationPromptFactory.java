package com.talented.buttie.simulation.service.llm;

import com.talented.buttie.simulation.domain.FinancialSnapshotVO;
import com.talented.buttie.simulation.dto.response.recommendation.CategoryExpenseAggregateResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class FinancialRecommendationPromptFactory {

    public String create(
        FinancialSnapshotVO snapshot,
        List<CategoryExpenseAggregateResponse> categoryExpenses,
        String userPrompt,
        RecommendationFocus focus
    ) {
        String categorySummary = categoryExpenses.stream()
            .map(item -> item.getExpenseCategory().name() + "(" + item.getExpenseCategory().getValue()
                + "): " + amount(item.getTotalAmount()) + "원")
            .collect(Collectors.joining(", "));

        return """
            사용자 맞춤 재정 추천 카드 생성기다.
            제공된 재정 데이터만 근거로 한국어 추천을 작성한다.
            사용자를 비난하거나 중독·낭비 등의 낙인 표현을 사용하지 않는다.
            금액과 생존 기간을 새로 계산하거나 보장하지 않는다.
            추천은 최대 3개이며, 월 단위로 실행 가능한 제안만 작성한다.
            
            반드시 JSON 객체만 반환한다. 마크다운이나 설명을 추가하지 않는다.
            JSON 형식:
            {
              "summary": "한두 문장 요약",
              "recommendations": [
                {
                  "actionType": "REDUCE_EXPENSE",
                  "title": "짧은 행동 제목",
                  "category": "ExpenseCategory enum 이름 또는 JOB_PREPARATION",
                  "suggestedMonthlyAmount": 0보다 큰 정수,
                  "reason": "한 문장 근거"
                }
              ]
            }
            REDUCE_EXPENSE의 category는 아래 소비 카테고리 enum 중 하나만 사용한다.
            FOOD, ALCOHOL_ENTERTAINMENT, CAFE_SNACK, JOB_PREPARATION, SHOPPING,
            HOBBY_LEISURE, HOUSING_COMMUNICATION, TRANSPORT_FUEL, HEALTH_FITNESS, OTHER_FINANCE
            ALLOCATE_SURPLUS는 절대 사용하지 않는다.
            
            재정 데이터:
            - 최근 3개월 월평균 수입: %s원
            - 최근 3개월 월평균 지출: %s원
            - 유동 자산: %d원
            - 현재 준비 가능 기간: %s개월
            - 재정 위험도: %s
            - 최근 3개월 카테고리별 지출 합계: %s
            - 사용자의 요청: %s
            - 추천 범위: %s
            """.formatted(
            amount(snapshot.getAvgMonthlyIncome()),
            amount(snapshot.getAvgMonthlyExpense()),
            snapshot.getLiquidAssets() == null ? 0 : snapshot.getLiquidAssets(),
            amount(snapshot.getCurrentPrepMonths()),
            snapshot.getRiskLevel(),
            categorySummary.isBlank() ? "분류된 지출 없음" : categorySummary,
            userPrompt == null || userPrompt.isBlank() ? "기본 재정 추천" : userPrompt,
            focus.description()
        );
    }

    private String amount(BigDecimal value) {
        return value == null ? "0" : value.toPlainString();
    }
}
