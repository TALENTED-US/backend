package com.talented.buttie.simulation.service.llm;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.simulation.exception.SimulationErrorCode;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AiRecommendationPromptScopeValidator {

    private static final List<String> FINANCIAL_KEYWORDS = List.of(
        "재정", "예산", "저축", "지출", "소비", "생활비", "수입", "소득", "돈", "자산", "부채",
        "대출", "카드", "식비", "쇼핑", "카페", "간식", "유흥", "취미", "교통", "통신", "월세",
        "주거", "알바", "부업", "일자리", "취업", "면접", "자격증", "교육", "정책", "지원금", "청년"
    );
    private static final List<String> EXPENSE_INTENT_KEYWORDS = List.of("줄여", "절약", "절감", "아껴");
    private static final List<String> INCOME_INTENT_KEYWORDS = List.of("늘려", "벌고", "구해", "일하고");

    public void validate(String prompt) {
        String normalized = prompt == null ? "" : prompt.trim();
        boolean inScope = containsAny(normalized, FINANCIAL_KEYWORDS);
        validate(inScope);
    }

    public void validateExpense(String prompt) {
        validate(containsAny(normalize(prompt), FINANCIAL_KEYWORDS)
            || containsAny(normalize(prompt), EXPENSE_INTENT_KEYWORDS));
    }

    public void validateIncome(String prompt) {
        validate(containsAny(normalize(prompt), FINANCIAL_KEYWORDS)
            || containsAny(normalize(prompt), INCOME_INTENT_KEYWORDS));
    }

    public void validatePolicy(String prompt) {
        validate(containsAny(normalize(prompt), FINANCIAL_KEYWORDS));
    }

    private void validate(boolean inScope) {
        if (!inScope) {
            throw ApplicationException.from(SimulationErrorCode.AI_PROMPT_OUT_OF_SCOPE);
        }
    }

    private boolean containsAny(String value, List<String> keywords) {
        return keywords.stream().anyMatch(value::contains);
    }

    private String normalize(String prompt) {
        return prompt == null ? "" : prompt.trim();
    }
}
