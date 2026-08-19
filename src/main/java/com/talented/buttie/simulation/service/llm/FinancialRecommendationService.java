package com.talented.buttie.simulation.service.llm;

import com.github.benmanes.caffeine.cache.Cache;
import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.common.util.CacheKeyUtils;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.simulation.domain.FinancialSnapshotVO;
import com.talented.buttie.simulation.dto.response.recommendation.CategoryExpenseAggregateResponse;
import com.talented.buttie.simulation.dto.response.recommendation.FinancialRecommendationResponse;
import com.talented.buttie.simulation.dto.response.recommendation.OpenAiRecommendationResult;
import com.talented.buttie.simulation.exception.AnalysisErrorCode;
import com.talented.buttie.simulation.exception.SimulationErrorCode;
import com.talented.buttie.simulation.mapper.FinancialSnapshotMapper;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FinancialRecommendationService {

    private static final int ANALYSIS_MONTHS = 3;
    private static final int RECOMMENDATION_AMOUNT_UNIT = 100;

    private final FinancialSnapshotMapper financialSnapshotMapper;
    private final TransactionMapper transactionMapper;
    private final FinancialRecommendationPromptFactory promptFactory;
    private final OpenAiChatClient openAiChatClient;
    private final Cache<String, FinancialRecommendationResponse> financialRecommendationCache;

    public FinancialRecommendationResponse getRecommendations(Long userId, String userPrompt, RecommendationFocus focus) {
        FinancialSnapshotVO snapshot = financialSnapshotMapper.findLatestByUserId(userId);
        if (snapshot == null || snapshot.getSnapshotId() == null) {
            throw ApplicationException.from(AnalysisErrorCode.SNAPSHOT_NOT_FOUND);
        }

        String cacheKey = "financial-recommendation:v3:" + userId + ":" + snapshot.getSnapshotId()
            + ":" + focus.name() + ":" + CacheKeyUtils.sha256(normalize(userPrompt));
        FinancialRecommendationResponse cached = financialRecommendationCache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }

        LocalDate today = LocalDate.now();
        List<CategoryExpenseAggregateResponse> categoryExpenses = transactionMapper.aggregateExpenseByCategory(
            userId,
            today.minusMonths(ANALYSIS_MONTHS).atStartOfDay(),
            today.plusDays(1).atStartOfDay()
        );
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        Map<ExpenseCategory, Integer> previousMonthLimits = transactionMapper.aggregateExpenseByCategory(
                userId, previousMonth.atDay(1).atStartOfDay(), previousMonth.plusMonths(1).atDay(1).atStartOfDay())
            .stream()
            .filter(item -> item.getExpenseCategory() != null && item.getTotalAmount() != null)
            .collect(Collectors.toMap(CategoryExpenseAggregateResponse::getExpenseCategory,
                item -> item.getTotalAmount().intValue(), Integer::max));

        OpenAiRecommendationResult result = openAiChatClient.createFinancialRecommendation(
            promptFactory.create(snapshot, categoryExpenses, userPrompt, focus)
        );
        FinancialRecommendationResponse response = toResponse(result, previousMonthLimits, userPrompt);
        financialRecommendationCache.put(cacheKey, response);
        return response;
    }

    public void invalidateForUser(Long userId) {
        String keyPrefix = "financial-recommendation:v3:" + userId + ":";
        financialRecommendationCache.asMap().keySet().removeIf(key -> key.startsWith(keyPrefix));
    }

    private FinancialRecommendationResponse toResponse(
        OpenAiRecommendationResult result,
        Map<ExpenseCategory, Integer> previousMonthLimits,
        String userPrompt
    ) {
        if (result == null || result.getSummary() == null || result.getRecommendations() == null) {
            throw ApplicationException.from(SimulationErrorCode.AI_RECOMMENDATION_UNAVAILABLE);
        }

        List<FinancialRecommendationResponse.RecommendationItem> items = result.getRecommendations().stream()
            .map(item -> toItem(item, previousMonthLimits))
            .filter(item -> item != null)
            .collect(Collectors.toMap(
                item -> item.getActionType().name() + ":" + item.getCategory(),
                Function.identity(),
                (first, ignored) -> first,
                LinkedHashMap::new
            ))
            .values()
            .stream()
            .limit(3)
            .toList();

        if (items.isEmpty()) {
            throw ApplicationException.from(SimulationErrorCode.AI_RECOMMENDATION_UNAVAILABLE);
        }

        return FinancialRecommendationResponse.builder()
            .summary(appendTradeoffGuide(result.getSummary(), userPrompt))
            .recommendations(items)
            .build();
    }

    private String appendTradeoffGuide(String summary, String userPrompt) {
        ExpenseCategory protectedCategory = findProtectedExpenseCategory(userPrompt);
        if (protectedCategory == null) return summary;

        String categoryName = protectedCategory.getValue();
        return summary + " 다만 " + categoryName + " 지출을 유지하면 다른 절감안만으로 재정 균형을 맞추는 데 "
            + "한계가 있을 수 있어요. 다음 단계에서 추가 소득을 늘리거나, 필요하면 " + categoryName
            + " 지출 조정도 다시 검토해 보세요.";
    }

    private ExpenseCategory findProtectedExpenseCategory(String userPrompt) {
        String prompt = normalize(userPrompt);
        boolean refusesReduction = prompt.contains("줄이고 싶지 않")
            || prompt.contains("줄이기 싫")
            || prompt.contains("줄이지 않")
            || prompt.contains("유지하고 싶");
        if (!refusesReduction) return null;

        return java.util.Arrays.stream(ExpenseCategory.values())
            .filter(category -> prompt.contains(category.getValue()))
            .findFirst()
            .orElse(null);
    }

    private FinancialRecommendationResponse.RecommendationItem toItem(OpenAiRecommendationResult.Recommendation item,
        Map<ExpenseCategory, Integer> previousMonthLimits) {
        if (!isValid(item)) return null;
        int amount = item.getSuggestedMonthlyAmount();
        String title = item.getTitle();
        if (item.getActionType().name().equals("REDUCE_EXPENSE")) {
            try {
                ExpenseCategory category = ExpenseCategory.valueOf(item.getCategory());
                amount = Math.min(amount, previousMonthLimits.getOrDefault(category, 0));
                if (amount <= 0) return null;
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        amount = roundDownToThousand(amount);
        if (amount <= 0) return null;
        if (item.getActionType().name().equals("REDUCE_EXPENSE")) {
            ExpenseCategory category = ExpenseCategory.valueOf(item.getCategory());
            title = category.getValue() + " 월 " + String.format("%,d", amount) + "원 줄이기";
        }
        return FinancialRecommendationResponse.RecommendationItem.builder()
            .actionType(item.getActionType()).title(title).category(item.getCategory())
            .suggestedMonthlyAmount(amount).reason(item.getReason()).build();
    }

    private int roundDownToThousand(int amount) {
        return Math.max(0, amount / RECOMMENDATION_AMOUNT_UNIT * RECOMMENDATION_AMOUNT_UNIT);
    }

    private boolean isValid(OpenAiRecommendationResult.Recommendation item) {
        return item != null
            && item.getActionType() == com.talented.buttie.simulation.dto.response.recommendation.RecommendationActionType.REDUCE_EXPENSE
            && item.getTitle() != null && !item.getTitle().isBlank()
            && item.getCategory() != null && !item.getCategory().isBlank()
            && item.getSuggestedMonthlyAmount() != null && item.getSuggestedMonthlyAmount() > 0
            && item.getReason() != null && !item.getReason().isBlank();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
