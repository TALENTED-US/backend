package com.talented.buttie.catalog.service;

import com.talented.buttie.catalog.domain.AmountParseConfidence;
import com.talented.buttie.catalog.domain.PolicyStatus;
import com.talented.buttie.catalog.domain.PolicyVO;
import com.talented.buttie.catalog.external.PolicySupportAmountParser;
import com.talented.buttie.catalog.external.PolicySupportAmountParser.ParseResult;
import com.talented.buttie.catalog.external.vworld.RegionCodeCache;
import com.talented.buttie.catalog.external.youthcenter.YouthCenterCodeTable;
import com.talented.buttie.catalog.external.youthcenter.YouthCenterPolicyItem;
import com.talented.buttie.catalog.mapper.PolicyMapper;
import com.talented.buttie.catalog.mapper.PolicyRegionMapMapper;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 정책 한 건의 upsert + 지역 매핑 저장을 독립 트랜잭션으로 처리한다.
 * PolicyIngestionService의 배치 루프에서 이 빈을 통해(프록시 경유) 호출해야
 * 항목 하나가 실패해도 그 항목의 변경만 롤백되고 다른 항목에 영향이 없다.
 */
@Service
@RequiredArgsConstructor
public class PolicyUpsertService {

    private static final String EXTERNAL_SOURCE = "YOUTHCENTER";
    private static final DateTimeFormatter YMD_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RegionCodeCache regionCodeCache;
    private final PolicySupportAmountParser amountParser;
    private final PolicyMapper policyMapper;
    private final PolicyRegionMapMapper policyRegionMapMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void upsertPolicy(YouthCenterPolicyItem item) {
        ParseResult parseResult = amountParser.parse(item.plcySprtCn());
        LocalDate dueDate = parseDueDate(item.aplyYmd(), item.bizPrdEndYmd());
        AmountParseConfidence confidence = AmountParseConfidence.valueOf(parseResult.confidence());

        PolicyVO policy = PolicyVO.builder()
            .externalSource(EXTERNAL_SOURCE)
            .externalPolicyId(item.plcyNo())
            .policyName(item.plcyNm())
            .policyCategory(YouthCenterCodeTable.normalizeCategory(item.lclsfNm()))
            .policyMinAge(parseAge(item.sprtTrgtMinAge(), item.sprtTrgtAgeLmtYn()))
            .policyMaxAge(parseAge(item.sprtTrgtMaxAge(), item.sprtTrgtAgeLmtYn()))
            .policyRegion(resolveRepresentativeRegion(item.zipCd()))
            .policySupportAmount(parseResult.amount())
            .supportMonthCount(parseResult.supportMonthCount() == null ? 1 : parseResult.supportMonthCount())
            .dueDate(dueDate)
            .requiredDocument(item.sbmsnDcmntCn())
            .employmentPrepStatus(YouthCenterCodeTable.resolveEmploymentPrepStatus(item.jobCd()))
            .familyCount(1)
            .policyStatus(resolveStatus(dueDate, confidence))
            .policyUrl(item.aplyUrlAddr())
            .amountParseConfidence(confidence)
            .build();

        policyMapper.upsert(policy);

        saveRegionMap(policy.getPolicyId(), item.zipCd());
    }

    private void saveRegionMap(Long policyId, String zipCdRaw) {
        policyRegionMapMapper.deleteByPolicyId(policyId);
        if (zipCdRaw == null || zipCdRaw.isBlank()) {
            return;
        }

        for (String zipCd : zipCdRaw.split(",")) {
            String trimmed = zipCd.trim();
            if (trimmed.isBlank()) {
                continue;
            }
            policyRegionMapMapper.insert(policyId, trimmed, regionCodeCache.resolve(trimmed));
        }
    }

    private Integer parseAge(String ageStr, String ageLmtYn) {
        if (!"Y".equals(ageLmtYn) || ageStr == null || ageStr.isBlank()) {
            return null;
        }
        return Integer.parseInt(ageStr.trim());
    }

    private String resolveRepresentativeRegion(String zipCdRaw) {
        if (zipCdRaw == null || zipCdRaw.isBlank()) {
            return "전국";
        }
        String[] zipCds = zipCdRaw.split(",");
        String firstZipCd = zipCds[0].trim();
        String firstRegionName = regionCodeCache.resolve(firstZipCd);
        if (firstRegionName == null) {
            // 캐시 미확인 코드: "전국"으로 뭉개지 않고 코드 그대로 표시
            firstRegionName = firstZipCd;
        }
        if (zipCds.length == 1) {
            return firstRegionName;
        }
        return firstRegionName + " 외 " + (zipCds.length - 1) + "곳";
    }

    private LocalDate parseDueDate(String aplyYmd, String bizPrdEndYmd) {
        if (aplyYmd != null && !aplyYmd.isBlank()) {
            String[] parts = aplyYmd.split("~");
            String endYmd = parts[parts.length - 1].trim();
            LocalDate parsed = tryParseYmd(endYmd);
            if (parsed != null) {
                return parsed;
            }
        }
        if (bizPrdEndYmd != null && !bizPrdEndYmd.trim().isBlank()) {
            return tryParseYmd(bizPrdEndYmd.trim());
        }
        return null;
    }

    private LocalDate tryParseYmd(String ymd) {
        try {
            return LocalDate.parse(ymd, YMD_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 지원금액 신뢰도가 HIGH가 아니면(LOW/MANUAL) 관리자 검수 전까지 공개 목록(findAll)과
     * 시뮬레이션 적용 대상에서 제외한다. POLICY_STATUS='AVAILABLE' 조건으로 이미 걸러지는
     * 기존 조회 로직을 그대로 활용한다.
     */
    private PolicyStatus resolveStatus(LocalDate dueDate, AmountParseConfidence confidence) {
        if (confidence != AmountParseConfidence.HIGH) {
            return PolicyStatus.CLOSED;
        }
        if (dueDate == null) {
            return PolicyStatus.AVAILABLE;
        }
        return dueDate.isBefore(LocalDate.now()) ? PolicyStatus.CLOSED : PolicyStatus.AVAILABLE;
    }
}
