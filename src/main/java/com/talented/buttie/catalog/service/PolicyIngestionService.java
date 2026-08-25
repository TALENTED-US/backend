package com.talented.buttie.catalog.service;

import com.talented.buttie.catalog.external.youthcenter.YouthCenterPolicyClient;
import com.talented.buttie.catalog.external.youthcenter.YouthCenterPolicyItem;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyIngestionService {

    private final YouthCenterPolicyClient youthCenterPolicyClient;
    private final PolicyUpsertService policyUpsertService;

    @Scheduled(cron = "0 0 4 * * *")
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
                // 프록시(PolicyUpsertService)를 통해 호출해야 항목별로 별도 트랜잭션이 걸린다.
                policyUpsertService.upsertPolicy(item);
                successCount++;
            } catch (Exception e) {
                log.error("정책 저장 실패 plcyNo={}", item.plcyNo(), e);
                failCount++;
            }
        }
        log.info("온통청년 정책 수집 완료: 성공 {}건, 실패 {}건", successCount, failCount);
    }
}
