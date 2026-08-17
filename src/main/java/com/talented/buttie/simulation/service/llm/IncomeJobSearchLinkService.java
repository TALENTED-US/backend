package com.talented.buttie.simulation.service.llm;

import com.github.benmanes.caffeine.cache.Cache;
import com.talented.buttie.simulation.dto.response.recommendation.IncomeJobSearchResponse;
import com.talented.buttie.user.domain.EmploymentPreparationVO;
import com.talented.buttie.user.mapper.EmploymentPreparationMapper;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilderFactory;

@Service
@Slf4j
@RequiredArgsConstructor
public class IncomeJobSearchLinkService {

    private static final String WORK24_URL = "https://www.work24.go.kr/cm/openApi/call/wk/callOpenApiSvcInfo210L01.do";

    private final EmploymentPreparationMapper employmentPreparationMapper;
    private final Cache<String, IncomeJobSearchResponse> incomeJobSearchCache;
    private final RestTemplate restTemplate;

    @Value("${work24.api.key}")
    private String work24ApiKey;

    public IncomeJobSearchResponse create(Long userId, String prompt) {
        EmploymentPreparationVO preparation = employmentPreparationMapper.selectEmploymentPreparation(userId);
        String region = preparation == null || preparation.getEmploymentPrepRegion() == null
            ? "내 주변" : preparation.getEmploymentPrepRegion();
        String keyword = (region + " " + (prompt == null || prompt.isBlank() ? "단기 알바" : prompt)).trim();
        String cacheKey = "income-job-search:" + userId + ":" + Integer.toHexString(keyword.hashCode());
        IncomeJobSearchResponse cached = incomeJobSearchCache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }

        List<IncomeJobSearchResponse.JobPosting> jobs = searchJobs(keyword);
        IncomeJobSearchResponse response = IncomeJobSearchResponse.builder()
            .searchKeyword(keyword)
            .region(region)
            .notice(jobs.isEmpty() ? "조건에 맞는 공고가 없거나 고용24 조회에 실패했습니다." : "고용24의 최신 시간제·단기 공고입니다.")
            .links(List.of(
                IncomeJobSearchResponse.JobSearchLink.builder()
                    .platform("고용24")
                    .url("https://www.work24.go.kr")
                    .build()
            ))
            .jobs(jobs)
            .build();
        incomeJobSearchCache.put(cacheKey, response);
        return response;
    }

    private List<IncomeJobSearchResponse.JobPosting> searchJobs(String keyword) {
        if (work24ApiKey == null || work24ApiKey.isBlank()) return List.of();
        try {
            String url = UriComponentsBuilder.fromHttpUrl(WORK24_URL)
                .queryParam("authKey", work24ApiKey).queryParam("callTp", "L")
                .queryParam("returnType", "XML").queryParam("startPage", 1).queryParam("display", 10)
                .queryParam("empTp", "11|21").queryParam("empTpGb", "2")
                .queryParam("salTp", "H").queryParam("keyword", keyword).build().encode().toUriString();
            return parse(restTemplate.getForObject(url, String.class));
        } catch (Exception e) {
            log.warn("고용24 공고 조회 실패: {}", e.getMessage());
            return List.of();
        }
    }

    private List<IncomeJobSearchResponse.JobPosting> parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        NodeList wanted = document.getElementsByTagName("wanted");
        List<IncomeJobSearchResponse.JobPosting> jobs = new ArrayList<>();
        for (int i = 0; i < wanted.getLength(); i++) {
            Element item = (Element) wanted.item(i);
            jobs.add(IncomeJobSearchResponse.JobPosting.builder()
                .title(text(item, "title")).company(text(item, "company")).region(text(item, "region"))
                .pay(text(item, "salTpNm") + " " + text(item, "sal"))
                .employmentType(text(item, "empTpCd")).url(text(item, "wantedInfoUrl")).build());
        }
        return jobs;
    }

    private String text(Element element, String tag) {
        NodeList nodes = element.getElementsByTagName(tag);
        return nodes.getLength() == 0 ? null : nodes.item(0).getTextContent();
    }
}
