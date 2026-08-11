package com.talented.buttie.catalog.external;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class PolicySupportAmountParser {

    private static final Pattern AMOUNT_PATTERN =
        Pattern.compile("(\\d+)만원|(\\d{1,3}(?:,\\d{3})+)원");
    private static final Pattern MONTH_COUNT_PATTERN = Pattern.compile("(\\d{1,3})\\s*개월");
    private static final Set<String> EXCLUDE_KEYWORDS = Set.of("시간당", "생활임금", "일당", "시급");
    private static final Set<String> MONTHLY_KEYWORDS = Set.of("매월", "월별", "매달", "월 최대", "월 지원", "개월");
    private static final int EXCLUDE_LOOKBEHIND = 10;

    public ParseResult parse(String plcySprtCn) {
        if (plcySprtCn == null || plcySprtCn.isBlank()) {
            return ParseResult.manual();
        }

        List<Integer> candidates = extractCandidates(plcySprtCn);
        Set<Integer> distinctValues = Set.copyOf(candidates);
        if (distinctValues.size() != 1) {
            return ParseResult.manual();
        }

        int amount = candidates.get(0);
        boolean hasMonthlyKeyword = MONTHLY_KEYWORDS.stream().anyMatch(plcySprtCn::contains);
        if (!hasMonthlyKeyword) {
            return new ParseResult(amount, 1, "HIGH");
        }

        Matcher monthMatcher = MONTH_COUNT_PATTERN.matcher(plcySprtCn);
        if (monthMatcher.find()) {
            return new ParseResult(amount, Integer.parseInt(monthMatcher.group(1)), "HIGH");
        }

        return new ParseResult(amount, null, "LOW");
    }

    private List<Integer> extractCandidates(String text) {
        List<Integer> candidates = new ArrayList<>();
        Matcher matcher = AMOUNT_PATTERN.matcher(text);
        while (matcher.find()) {
            if (isExcluded(text, matcher.start())) {
                continue;
            }
            candidates.add(matcher.group(1) != null
                ? Integer.parseInt(matcher.group(1)) * 10_000
                : Integer.parseInt(matcher.group(2).replace(",", "")));
        }
        return candidates;
    }

    private boolean isExcluded(String text, int matchStart) {
        String lookbehind = text.substring(Math.max(0, matchStart - EXCLUDE_LOOKBEHIND), matchStart);
        return EXCLUDE_KEYWORDS.stream().anyMatch(lookbehind::contains);
    }

    public record ParseResult(Integer amount, Integer supportMonthCount, String confidence) {
        static ParseResult manual() {
            return new ParseResult(null, null, "MANUAL");
        }
    }
}
