package com.talented.buttie.catalog.external.youthcenter;

import com.talented.buttie.catalog.domain.PolicyCategory;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class YouthCenterCodeTable {

    private static final Map<String, String> EMPLOYMENT_PREP_STATUS_MAP = Map.ofEntries(
        Map.entry("0013001", "재직자"),
        Map.entry("0013002", "자영업자"),
        Map.entry("0013003", "미취업자"),
        Map.entry("0013004", "프리랜서"),
        Map.entry("0013005", "일용근로자"),
        Map.entry("0013006", "예비창업자"),
        Map.entry("0013007", "단기근로자"),
        Map.entry("0013008", "영농종사자"),
        Map.entry("0013009", "기타"),
        Map.entry("0013010", "")
    );

    private YouthCenterCodeTable() {
    }

    public static PolicyCategory normalizeCategory(String rawLclsfNm) {
        if (rawLclsfNm == null || rawLclsfNm.isBlank()) {
            return null;
        }
        if (rawLclsfNm.contains("일자리")) {
            return PolicyCategory.EMPLOYMENT;
        }
        if (rawLclsfNm.contains("주거")) {
            return PolicyCategory.HOUSING;
        }
        if (rawLclsfNm.contains("교육")) {
            return PolicyCategory.EDUCATION;
        }
        if (rawLclsfNm.contains("복지") || rawLclsfNm.contains("문화")) {
            return PolicyCategory.WELFARE;
        }
        if (rawLclsfNm.contains("참여")) {
            return PolicyCategory.YOUTH_SUPPORT;
        }
        log.warn("엑셀 코드표(정책대분류)에 없는 값 발견: {}", rawLclsfNm);
        return PolicyCategory.OTHER;
    }

    /**
     * jobCd는 zipCd처럼 콤마로 여러 개가 올 수 있다 (예: "0013008,0013009").
     */
    public static String resolveEmploymentPrepStatus(String rawJobCd) {
        if (rawJobCd == null || rawJobCd.isBlank()) {
            return null;
        }

        Set<String> statuses = new LinkedHashSet<>();
        for (String jobCd : rawJobCd.split(",")) {
            String trimmed = jobCd.trim();
            if (trimmed.isBlank()) {
                continue;
            }
            if (!EMPLOYMENT_PREP_STATUS_MAP.containsKey(trimmed)) {
                log.warn("알 수 없는 jobCd 값: {}", trimmed);
                continue;
            }
            String status = EMPLOYMENT_PREP_STATUS_MAP.get(trimmed);
            if (!status.isEmpty()) {
                statuses.add(status);
            }
        }
        return statuses.isEmpty() ? null : String.join(",", statuses);
    }
}
