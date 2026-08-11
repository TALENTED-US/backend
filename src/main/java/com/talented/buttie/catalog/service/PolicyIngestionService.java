package com.talented.buttie.catalog.service;

import com.talented.buttie.catalog.domain.AmountParseConfidence;
import com.talented.buttie.catalog.domain.PolicyStatus;
import com.talented.buttie.catalog.domain.PolicyVO;
import com.talented.buttie.catalog.external.PolicySupportAmountParser;
import com.talented.buttie.catalog.external.PolicySupportAmountParser.ParseResult;
import com.talented.buttie.catalog.external.vworld.RegionCodeCache;
import com.talented.buttie.catalog.external.youthcenter.YouthCenterCodeTable;
import com.talented.buttie.catalog.external.youthcenter.YouthCenterPolicyClient;
import com.talented.buttie.catalog.external.youthcenter.YouthCenterPolicyItem;
import com.talented.buttie.catalog.mapper.PolicyMapper;
import com.talented.buttie.catalog.mapper.PolicyRegionMapMapper;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyIngestionService {

    private static final String EXTERNAL_SOURCE = "YOUTHCENTER";
    private static final DateTimeFormatter YMD_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final YouthCenterPolicyClient youthCenterPolicyClient;
    private final RegionCodeCache regionCodeCache;
    private final PolicySupportAmountParser amountParser;
    private final PolicyMapper policyMapper;
    private final PolicyRegionMapMapper policyRegionMapMapper;

    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void ingestYouthCenterPolicies() {
        List<YouthCenterPolicyItem> items;
        try {
            items = youthCenterPolicyClient.fetchAll();
        } catch (Exception e) {
            log.error("온통청년 정책 수집 실패", e);
            return;
        }

        int successCount = 0, failCount = 0;
        for (YouthCenterPolicyItem item : items) {
            try {
                upsertPolicy(item);
                successCount++;
            } catch (Exception e) {
                log.error("정책 저장 실패 plcyNo={}", item.plcyNo(), e);
                failCount++;
            }
        }
        log.info("온통청년 정책 수집 완료: 성공 {}건, 실패 {}건", successCount, failCount);
    }

    void upsertPolicy(YouthCenterPolicyItem item) {
        ParseResult parseResult = amountParser.parse(item.plcySprtCn());
        LocalDate dueDate = parseDueDate(item.aplyYmd(), item.bizPrdEndYmd());

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
            .policyStatus(resolveStatus(dueDate))
            .policyUrl(item.aplyUrlAddr())
            .amountParseConfidence(AmountParseConfidence.valueOf(parseResult.confidence()))
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
        String firstRegionName = regionCodeCache.resolve(zipCds[0].trim());
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

    private PolicyStatus resolveStatus(LocalDate dueDate) {
        if (dueDate == null) {
            return PolicyStatus.AVAILABLE;
        }
        return dueDate.isBefore(LocalDate.now()) ? PolicyStatus.CLOSED : PolicyStatus.AVAILABLE;
    }
}
