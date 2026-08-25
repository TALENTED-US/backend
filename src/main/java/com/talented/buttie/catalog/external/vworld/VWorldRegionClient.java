package com.talented.buttie.catalog.external.vworld;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class VWorldRegionClient {

    private static final String SIDO_URL = "https://api.vworld.kr/ned/data/admCodeList";
    private static final String SIGUNGU_URL = "https://api.vworld.kr/ned/data/admSiList";
    private static final int NUM_OF_ROWS = 200;
    // ponytail: 정부망 API가 RestTemplate 기본 다중 Accept 헤더에 500을 반환하는 경우 대응 (온통청년과 동일 이슈, curl로 확인)
    private static final String USER_AGENT =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36";

    @Value("${vworld.api.key}")
    private String apiKey;

    @Value("${vworld.domain}")
    private String domain;

    private final RestTemplate restTemplate;

    public List<AdmCodeItem> fetchSidoList() {
        if (apiKey == null || apiKey.isBlank()) {
            log.info("VWORLD_API_KEY가 없어 행정구역코드 조회를 건너뜁니다.");
            return List.of();
        }
        String url = UriComponentsBuilder.fromHttpUrl(SIDO_URL)
            .queryParam("key", apiKey)
            .queryParam("domain", domain)
            .queryParam("format", "json")
            .queryParam("numOfRows", NUM_OF_ROWS)
            .queryParam("pageNo", 1)
            .toUriString();
        return fetch(url);
    }

    public List<AdmCodeItem> fetchSigunguList(String sidoAdmCode) {
        if (apiKey == null || apiKey.isBlank()) {
            return List.of();
        }
        String url = UriComponentsBuilder.fromHttpUrl(SIGUNGU_URL)
            .queryParam("admCode", sidoAdmCode)
            .queryParam("key", apiKey)
            .queryParam("domain", domain)
            .queryParam("format", "json")
            .queryParam("numOfRows", NUM_OF_ROWS)
            .queryParam("pageNo", 1)
            .toUriString();
        return fetch(url);
    }

    private List<AdmCodeItem> fetch(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, USER_AGENT);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        VWorldAdmResponse response = restTemplate
            .exchange(url, HttpMethod.GET, new HttpEntity<>(headers), VWorldAdmResponse.class)
            .getBody();
        if (response == null || response.admVOList() == null) {
            return List.of();
        }
        VWorldAdmResponse.Body body = response.admVOList();
        if (body.error() != null && !body.error().isBlank()) {
            log.warn("VWorld API 인증 실패: {} - {}", body.error(), body.message());
            return List.of();
        }
        if (body.admVOList() == null) {
            return List.of();
        }
        return body.admVOList();
    }
}
