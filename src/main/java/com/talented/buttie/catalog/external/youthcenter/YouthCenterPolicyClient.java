package com.talented.buttie.catalog.external.youthcenter;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class YouthCenterPolicyClient {

    private static final String BASE_URL = "https://www.youthcenter.go.kr/go/ythip/getPlcy";
    private static final int PAGE_SIZE = 100;
    // ponytail: youthcenter.go.kr가 RestTemplate 기본 Accept("application/json, application/*+json")
    // 다중 타입 헤더에 500을 반환함 (curl로 재현 확인). 단일 application/json만 보내면 정상 동작.
    private static final String USER_AGENT =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36";

    @Value("${youth.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public List<YouthCenterPolicyItem> fetchAll() {
        List<YouthCenterPolicyItem> items = new ArrayList<>();
        int pageNum = 1;
        int totCount;

        do {
            YouthCenterPolicyResponse response = fetchPage(pageNum);
            YouthCenterPolicyResponse.Result result = response.result();
            if (result == null || result.youthPolicyList() == null) {
                break;
            }
            items.addAll(result.youthPolicyList());
            totCount = result.pagging().totCount();
            pageNum++;
        } while ((long) (pageNum - 1) * PAGE_SIZE < totCount);

        return items;
    }

    private YouthCenterPolicyResponse fetchPage(int pageNum) {
        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
            .queryParam("apiKeyNm", apiKey)
            .queryParam("pageNum", pageNum)
            .queryParam("pageSize", PAGE_SIZE)
            .queryParam("rtnType", "json")
            .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, USER_AGENT);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), YouthCenterPolicyResponse.class)
            .getBody();
    }
}
