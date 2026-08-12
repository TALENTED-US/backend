package com.talented.buttie.mydata.service;

import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.ledger.domain.TransactionType;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import com.talented.buttie.mydata.dto.response.FixedExpenseCandidateResponse;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FixedExpenseCandidateService {

    private static final Set<ExpenseCategory> RECURRING_CATEGORIES = Set.of(
        ExpenseCategory.SUBSCRIPTION,
        ExpenseCategory.COMMUNICATION,
        ExpenseCategory.HOUSING
    );
    private static final List<String> RECURRING_KEYWORDS = List.of(
        "구독", "넷플릭스", "유튜브", "보험", "월세", "공과금", "정기권"
    );

    private final TransactionMapper transactionMapper;
    private final PKCrypto pkCrypto;

    public List<FixedExpenseCandidateResponse> findCandidates(Long userId) {
        Map<String, List<TransactionVO>> grouped = new LinkedHashMap<>();
        transactionMapper.findExternalTransactionsForAnalysis(userId).stream()
            .filter(transaction -> transaction.getTransactionType() == TransactionType.EXPENSE)
            .filter(transaction -> !Boolean.TRUE.equals(transaction.getAnalysisExcluded()))
            .filter(transaction -> transaction.getTransactionId() != null)
            .filter(transaction -> transaction.getTransactionAt() != null)
            .filter(transaction -> transaction.getTransactionAmount() != null)
            .filter(this::isRecurringCandidate)
            .forEach(transaction -> grouped.computeIfAbsent(
                candidateKey(transaction),
                ignored -> new ArrayList<>()
            ).add(transaction));

        return grouped.values().stream()
            .filter(this::appearsInMultipleMonths)
            .map(this::toResponse)
            .sorted(Comparator.comparing(
                FixedExpenseCandidateResponse::expectedAmount
            ).reversed())
            .collect(Collectors.toList());
    }

    private boolean isRecurringCandidate(TransactionVO transaction) {
        if (transaction.getExpenseCategory() != null
            && RECURRING_CATEGORIES.contains(transaction.getExpenseCategory())) {
            return true;
        }
        String content = normalize(transaction.getTransactionContent());
        return RECURRING_KEYWORDS.stream().anyMatch(content::contains);
    }

    private boolean appearsInMultipleMonths(List<TransactionVO> transactions) {
        return transactions.stream()
            .filter(transaction -> transaction.getTransactionAt() != null)
            .map(transaction -> YearMonth.from(transaction.getTransactionAt()))
            .distinct()
            .count() >= 2;
    }

    private FixedExpenseCandidateResponse toResponse(List<TransactionVO> transactions) {
        TransactionVO latest = transactions.stream()
            .max(Comparator.comparing(TransactionVO::getTransactionAt))
            .orElseThrow();
        int expectedAmount = (int) Math.round(transactions.stream()
            .mapToInt(TransactionVO::getTransactionAmount)
            .average()
            .orElse(0));

        return new FixedExpenseCandidateResponse(
            pkCrypto.encryptValue(latest.getTransactionId()),
            latest.getTransactionContent(),
            latest.getExpenseCategory(),
            latest.getTransactionSource(),
            expectedAmount,
            latest.getTransactionAt().getDayOfMonth(),
            transactions.size()
        );
    }

    private String candidateKey(TransactionVO transaction) {
        return normalize(transaction.getTransactionContent())
            + "|" + transaction.getExpenseCategory()
            + "|" + transaction.getTransactionSource();
    }

    private String normalize(String value) {
        return value == null
            ? ""
            : value.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }
}
