package com.talented.buttie.simulation.service.llm;

import com.github.benmanes.caffeine.cache.Cache;
import com.talented.buttie.common.exception.ApplicationException;
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
import java.util.List;
import java.util.Map;
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

        String cacheKey = "financial-recommendation:" + userId + ":" + snapshot.getSnapshotId()
            + ":" + focus.name() + ":" + Integer.toHexString(normalize(userPrompt).hashCode());
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
        FinancialRecommendationResponse response = toResponse(result, previousMonthLimits);
        financialRecommendationCache.put(cacheKey, response);
        return response;
    }

    private FinancialRecommendationResponse toResponse(OpenAiRecommendationResult result, Map<ExpenseCategory, Integer> previousMonthLimits) {
        if (result == null || result.getSummary() == null || result.getRecommendations() == null) {
            throw ApplicationException.from(SimulationErrorCode.AI_RECOMMENDATION_UNAVAILABLE);
        }

        List<FinancialRecommendationResponse.RecommendationItem> items = result.getRecommendations().stream()
            .map(item -> toItem(item, previousMonthLimits))
            .filter(item -> item != null)
            .limit(3)
            .toList();

        if (items.isEmpty()) {
            throw ApplicationException.from(SimulationErrorCode.AI_RECOMMENDATION_UNAVAILABLE);
        }

        return FinancialRecommendationResponse.builder()
            .summary(result.getSummary())
            .recommendations(items)
            .build();
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
