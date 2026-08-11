package com.talented.buttie.catalog.external.vworld;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegionCodeCache {

    private static final String NATIONWIDE = "전국";

    private final VWorldRegionClient vWorldRegionClient;

    private volatile Map<String, String> zipCdToRegionName = Map.of();

    @PostConstruct
    public void init() {
        refresh();
    }

    @Scheduled(cron = "0 0 3 1 * *")
    public void refresh() {
        try {
            zipCdToRegionName = buildCache();
            log.info("VWorld 행정구역코드 캐시 갱신 완료: {}건", zipCdToRegionName.size());
        } catch (Exception e) {
            log.error("VWorld 행정구역코드 캐시 갱신 실패, 기존 캐시 유지", e);
        }
    }

    public String resolve(String zipCd) {
        return zipCdToRegionName.getOrDefault(zipCd, NATIONWIDE);
    }

    private Map<String, String> buildCache() {
        Map<String, String> cache = new HashMap<>();
        List<AdmCodeItem> sidoList = vWorldRegionClient.fetchSidoList();

        for (AdmCodeItem sido : sidoList) {
            cache.put(sido.admCode(), sido.admCodeNm());
            cache.put(sido.admCode() + "000", sido.admCodeNm());

            // ponytail: 시군구 호출 하나가 타임아웃/일시 장애로 실패해도 나머지 시/도 캐싱은 계속 진행
            try {
                List<AdmCodeItem> sigunguList = vWorldRegionClient.fetchSigunguList(sido.admCode());
                for (AdmCodeItem sigungu : sigunguList) {
                    cache.put(sigungu.admCode(), sigungu.lowestAdmCodeNm());
                }
            } catch (Exception e) {
                log.warn("시군구 조회 실패, 해당 시/도({}) 스킵: {}", sido.admCodeNm(), e.getMessage());
            }
        }
        return cache;
    }
}
