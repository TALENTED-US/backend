package com.talented.buttie.simulation.service.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.simulation.dto.response.recommendation.OpenAiRecommendationResult;
import com.talented.buttie.simulation.dto.response.recommendation.PolicyRecommendationReasonResult;
import com.talented.buttie.simulation.exception.SimulationErrorCode;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
@RequiredArgsConstructor
public class OpenAiChatClient {

    private static final String RESPONSES_URL = "https://api.openai.com/v1/responses";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    public OpenAiRecommendationResult createFinancialRecommendation(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw ApplicationException.from(SimulationErrorCode.AI_RECOMMENDATION_UNAVAILABLE);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> request = Map.of(
            "model", model,
            "store", false,
            "input", List.of(Map.of(
                "role", "developer",
                "content", List.of(Map.of("type", "input_text", "text", prompt))
            )),
            "text", Map.of("format", Map.of("type", "json_object"))
        );

        try {
            Map<?, ?> response = restTemplate.postForObject(
                RESPONSES_URL,
                new HttpEntity<>(request, headers),
                Map.class
            );
            return objectMapper.readValue(extractOutputText(response), OpenAiRecommendationResult.class);
        } catch (HttpStatusCodeException e) {
            log.warn("OpenAI 재정 추천 요청 실패: status={}, response={}",
                e.getStatusCode(), abbreviate(e.getResponseBodyAsString()));
            throw ApplicationException.from(SimulationErrorCode.AI_RECOMMENDATION_UNAVAILABLE);
        } catch (RestClientException | JsonProcessingException | IllegalStateException e) {
            log.warn("OpenAI 재정 추천 요청 또는 응답 처리 실패: {}", e.getMessage());
            throw ApplicationException.from(SimulationErrorCode.AI_RECOMMENDATION_UNAVAILABLE);
        }
    }

    public PolicyRecommendationReasonResult createPolicyRecommendationReasons(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            return null;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> request = Map.of(
            "model", model,
            "store", false,
            "input", List.of(Map.of(
                "role", "developer",
                "content", List.of(Map.of("type", "input_text", "text", prompt))
            )),
            "text", Map.of("format", Map.of("type", "json_object"))
        );

        try {
            Map<?, ?> response = restTemplate.postForObject(
                RESPONSES_URL,
                new HttpEntity<>(request, headers),
                Map.class
            );
            return objectMapper.readValue(extractOutputText(response), PolicyRecommendationReasonResult.class);
        } catch (RestClientException | JsonProcessingException | IllegalStateException e) {
            log.warn("OpenAI 정책 추천 이유 생성 실패: exceptionType={}", e.getClass().getSimpleName());
            return null;
        }
    }

    private String abbreviate(String value) {
        if (value == null || value.length() <= 1_000) {
            return value;
        }
        return value.substring(0, 1_000) + "...";
    }

    private String extractOutputText(Map<?, ?> response) {
        if (response == null || !(response.get("output") instanceof List<?> output)) {
            throw new IllegalStateException("OpenAI Responses API output is missing");
        }

        for (Object outputItem : output) {
            if (!(outputItem instanceof Map<?, ?> item)
                || !(item.get("content") instanceof List<?> contents)) {
                continue;
            }
            for (Object content : contents) {
                if (content instanceof Map<?, ?> value
                    && "output_text".equals(value.get("type"))
                    && value.get("text") instanceof String text) {
                    return text;
                }
            }
        }
        throw new IllegalStateException("OpenAI Responses API output text is missing");
    }
}
