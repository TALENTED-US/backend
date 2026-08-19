package com.talented.buttie.simulation.service.llm;

import com.talented.buttie.catalog.dto.response.PolicyResponse;
import com.talented.buttie.simulation.domain.FinancialSnapshotVO;
import com.talented.buttie.user.domain.EmploymentPreparationVO;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class PolicyRecommendationPromptFactory {

    public String create(
        EmploymentPreparationVO preparation,
        FinancialSnapshotVO snapshot,
        List<PolicyResponse> policies
    ) {
        String policySummary = java.util.stream.IntStream.range(0, policies.size())
            .mapToObj(index -> {
                PolicyResponse policy = policies.get(index);
                return "[%d] 정책명=%s, 지원금=%s, 지원월수=%s, 마감일=%s, 제출·확인 조건=%s".formatted(index,
                    policy.policyName(), value(policy.policySupportAmount()),
                    value(policy.supportMonthCount()), value(policy.dueDate()),
                    truncate(policy.requiredDocument()));
            })
            .collect(Collectors.joining("\n"));

        return """
            너는 정책 추천 이유를 작성하는 보조 도구다.
            아래 정책들은 이미 DB에서 사용자 지역, 나이, 취업 준비 상태 기준으로 필터링되어 추천 대상이 확정됐다.
            정책을 새로 추천하거나, 제공되지 않은 지원 조건·금액·신청 가능 여부를 추측하지 않는다.
            각 정책마다 사용자의 조건과 정책의 제공 정보만 근거로 1~2문장의 구체적인 한국어 추천 이유를 작성한다.

            작성 규칙:
            1. recommendationReason은 반드시 [가장 적합], [우선 검토], [추가 조건 확인] 중 하나로 시작한다.
            2. 사용자 조건(지역, 나이, 취업 준비 상태, 수입·지출·자산) 중 확인 가능한 항목을 최소 1개 언급한다.
            3. 정책 정보(정책명, 지원금, 지원 기간, 제출·확인 조건) 중 최소 1개를 구체적으로 언급한다.
            4. "사용자의 [조건]을 고려했을 때, [정책 정보]에 부합할 수 있으므로 [기대 효과]를 위해 신청을 검토하는 것을 추천합니다"와 비슷한 흐름으로 작성한다.
            5. 면접 참여, 소득 기준, 거주 기간처럼 제공된 사용자 정보만으로 확인할 수 없는 추가 자격이 있으면 [추가 조건 확인]으로 시작하고, 확인이 필요한 항목을 명시한다.
            6. 제공되지 않은 사실을 만들어 "면접을 보지 않았다", "자격이 확정됐다"라고 단정하지 않는다.
            7. DB 필터를 통과한 정책만 전달되므로, 근거 없이 [부적합] 또는 다른 정책 추천을 작성하지 않는다.

            반드시 JSON 객체만 반환한다.
            형식:
            {"reasons":[{"index":0,"recommendationReason":"한 문장"}]}

            사용자 조건:
            - 지역: %s
            - 나이: %s
            - 취업 준비 상태: %s
            - 최근 3개월 월평균 수입: %s원
            - 최근 3개월 월평균 지출: %s원
            - 유동 자산: %s원

            필터링된 정책:
            %s
            """.formatted(
            preparation == null ? "미입력" : value(preparation.getEmploymentPrepRegion()),
            resolveAge(preparation),
            preparation == null || preparation.getEmploymentPrepType() == null
                ? "미입력" : preparation.getEmploymentPrepType().name(),
            snapshot == null ? "미입력" : value(snapshot.getAvgMonthlyIncome()),
            snapshot == null ? "미입력" : value(snapshot.getAvgMonthlyExpense()),
            snapshot == null ? "미입력" : value(snapshot.getLiquidAssets()),
            policySummary
        );
    }

    private String resolveAge(EmploymentPreparationVO preparation) {
        return preparation == null || preparation.getBirthDate() == null
            ? "미입력" : String.valueOf(Period.between(preparation.getBirthDate(), LocalDate.now()).getYears());
    }

    private String value(Object value) {
        return value == null ? "미입력" : value.toString();
    }

    private String truncate(String value) {
        if (value == null || value.isBlank()) return "미입력";
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 240 ? normalized : normalized.substring(0, 240) + "...";
    }
}
