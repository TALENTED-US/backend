package com.talented.buttie.mydata.client;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.mydata.client.dto.MydataAccountData;
import com.talented.buttie.mydata.client.dto.MydataAccountTransactionData;
import com.talented.buttie.mydata.client.dto.MydataAccountTransactionsResponse;
import com.talented.buttie.mydata.client.dto.MydataAuthorizationResponse;
import com.talented.buttie.mydata.client.dto.MydataBankAccountsResponse;
import com.talented.buttie.mydata.client.dto.MydataCardData;
import com.talented.buttie.mydata.client.dto.MydataCardApprovalData;
import com.talented.buttie.mydata.client.dto.MydataCardApprovalsResponse;
import com.talented.buttie.mydata.client.dto.MydataCardsResponse;
import com.talented.buttie.mydata.client.dto.MydataTokenResponse;
import com.talented.buttie.mydata.exception.MydataErrorCode;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class MockMydataApiClient implements MydataApiClient {

    private static final String SUCCESS_CODE = "00000";
    private static final String DEBIT_CARD_TYPE = "02";

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String orgCode;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String appScheme;

    public MockMydataApiClient(
        RestTemplate restTemplate,
        @Value("${mydata.mock.base-url}") String baseUrl,
        @Value("${mydata.mock.org-code}") String orgCode,
        @Value("${mydata.mock.client-id}") String clientId,
        @Value("${mydata.mock.client-secret}") String clientSecret,
        @Value("${mydata.mock.redirect-uri}") String redirectUri,
        @Value("${mydata.mock.app-scheme}") String appScheme
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.orgCode = orgCode;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.appScheme = appScheme;
    }

    @Override
    public MydataAuthorizationResponse authorize(Long userId) {
        String state = UUID.randomUUID().toString().replace("-", "");
        String uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
            .path("/v2/oauth/2.0/authorize")
            .queryParam("org_code", orgCode)
            .queryParam("response_type", "code")
            .queryParam("client_id", clientId)
            .queryParam("redirect_uri", redirectUri)
            .queryParam("app_scheme", appScheme)
            .queryParam("state", state)
            .build()
            .encode()
            .toUriString();

        HttpHeaders headers = createCommonHeaders(userId);
        headers.set("x-user-ci", String.valueOf(userId));

        try {
            MydataAuthorizationResponse body = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                new HttpEntity<Void>(headers),
                MydataAuthorizationResponse.class
            ).getBody();
            if (body == null || body.getCode() == null || !state.equals(body.getState())) {
                throw ApplicationException.from(MydataErrorCode.MYDATA_AUTHORIZATION_FAILED);
            }
            return body;
        } catch (RestClientException exception) {
            throw ApplicationException.from(MydataErrorCode.MYDATA_AUTHORIZATION_FAILED);
        }
    }

    @Override
    public MydataTokenResponse issueToken(String authorizationCode) {
        HttpHeaders headers = createCommonHeaders(null);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("org_code", orgCode);
        form.add("grant_type", "authorization_code");
        form.add("code", authorizationCode);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("redirect_uri", redirectUri);

        try {
            MydataTokenResponse body = restTemplate.exchange(
                baseUrl + "/v2/oauth/2.0/token",
                HttpMethod.POST,
                new HttpEntity<MultiValueMap<String, String>>(form, headers),
                MydataTokenResponse.class
            ).getBody();
            if (body == null || body.getRefreshToken() == null
                || body.getRefreshTokenExpiresIn() == null) {
                throw ApplicationException.from(MydataErrorCode.MYDATA_TOKEN_ISSUE_FAILED);
            }
            return body;
        } catch (RestClientException exception) {
            throw ApplicationException.from(MydataErrorCode.MYDATA_TOKEN_ISSUE_FAILED);
        }
    }

    @Override
    public List<MydataAccountData> getAccounts(Long userId) {
        int mockUserKey = toMockUserKey(userId);
        String uri = baseUrl + "/v2/bank/accounts";

        try {
            ResponseEntity<MydataBankAccountsResponse> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                createRequestEntity(mockUserKey),
                MydataBankAccountsResponse.class
            );
            MydataBankAccountsResponse body = response.getBody();
            validateResponse(body == null ? null : body.getRspCode());
            return body.getAccountList() == null
                ? Collections.emptyList()
                : body.getAccountList();
        } catch (RestClientException exception) {
            throw ApplicationException.from(MydataErrorCode.MYDATA_MOCK_API_FAILED);
        }
    }

    @Override
    public List<MydataCardData> getDebitCards(Long userId) {
        int mockUserKey = toMockUserKey(userId);
        String uri = baseUrl + "/v2/card/cards";

        try {
            ResponseEntity<MydataCardsResponse> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                createRequestEntity(mockUserKey),
                MydataCardsResponse.class
            );
            MydataCardsResponse body = response.getBody();
            validateResponse(body == null ? null : body.getRspCode());
            if (body.getCardList() == null) {
                return Collections.emptyList();
            }
            return body.getCardList().stream()
                .filter(Objects::nonNull)
                .filter(card -> DEBIT_CARD_TYPE.equals(card.getCardType()))
                .collect(Collectors.toList());
        } catch (RestClientException exception) {
            throw ApplicationException.from(MydataErrorCode.MYDATA_MOCK_API_FAILED);
        }
    }

    @Override
    public List<MydataAccountTransactionData> getAccountTransactions(
        Long userId,
        String externalAccountId
    ) {
        int mockUserKey = toMockUserKey(userId);
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("org_code", orgCode);
        requestBody.put("account_num", externalAccountId);
        requestBody.put("from_date", "20260601");
        requestBody.put("to_date", "20260831");
        requestBody.put("limit", 500);

        HttpHeaders headers = createCommonHeaders((long) mockUserKey);
        headers.setBearerAuth("mock-access-token-user-" + mockUserKey);
        headers.set("x-api-type", "regular");
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        try {
            MydataAccountTransactionsResponse body = restTemplate.exchange(
                baseUrl + "/v2/bank/accounts/deposit/transactions",
                HttpMethod.POST,
                new HttpEntity<Map<String, Object>>(requestBody, headers),
                MydataAccountTransactionsResponse.class
            ).getBody();
            validateResponse(body == null ? null : body.getResponseCode());
            return body.getTransactions() == null
                ? Collections.emptyList()
                : body.getTransactions();
        } catch (RestClientException exception) {
            throw ApplicationException.from(MydataErrorCode.MYDATA_MOCK_API_FAILED);
        }
    }

    @Override
    public List<MydataCardApprovalData> getCardApprovals(
        Long userId,
        String externalCardId
    ) {
        int mockUserKey = toMockUserKey(userId);
        String uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
            .pathSegment("v2", "card", "cards", externalCardId, "approval-domestic")
            .build()
            .encode()
            .toUriString();

        try {
            MydataCardApprovalsResponse body = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                createRequestEntity(mockUserKey),
                MydataCardApprovalsResponse.class
            ).getBody();
            validateResponse(body == null ? null : body.getResponseCode());
            return body.getApprovals() == null
                ? Collections.emptyList()
                : body.getApprovals();
        } catch (RestClientException exception) {
            throw ApplicationException.from(MydataErrorCode.MYDATA_MOCK_API_FAILED);
        }
    }

    private HttpEntity<Void> createRequestEntity(int mockUserKey) {
        HttpHeaders headers = createCommonHeaders((long) mockUserKey);
        headers.setBearerAuth("mock-access-token-user-" + mockUserKey);
        headers.set("x-api-type", "regular");
        return new HttpEntity<>(headers);
    }

    private HttpHeaders createCommonHeaders(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        long transactionKey = userId == null ? 0L : Math.floorMod(userId, 1_000_000_000_000L);
        headers.set("x-api-tran-id", String.format("BUTTIE-MOCK-%013d", transactionKey));
        return headers;
    }

    private int toMockUserKey(Long userId) {
        return Math.floorMod(userId, 10);
    }

    private void validateResponse(String responseCode) {
        if (!SUCCESS_CODE.equals(responseCode)) {
            throw ApplicationException.from(MydataErrorCode.MYDATA_MOCK_API_FAILED);
        }
    }
}
