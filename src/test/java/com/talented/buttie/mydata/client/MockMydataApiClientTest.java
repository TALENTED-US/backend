package com.talented.buttie.mydata.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.talented.buttie.mydata.client.dto.MydataAccountData;
import com.talented.buttie.mydata.client.dto.MydataAccountTransactionData;
import com.talented.buttie.mydata.client.dto.MydataCardApprovalData;
import com.talented.buttie.mydata.client.dto.MydataCardData;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class MockMydataApiClientTest {

    private MockRestServiceServer server;
    private MockMydataApiClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        client = new MockMydataApiClient(
            restTemplate,
            "http://localhost:3000",
            "MOCK001",
            "buttie",
            "mock-secret",
            "http://localhost:8080/api/mydata/callback",
            "buttie"
        );
    }

    @Test
    void userId_마지막_자리로_계좌를_조회한다() {
        server.expect(requestTo("http://localhost:3000/v2/bank/accounts"))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer mock-access-token-user-1"))
            .andRespond(withSuccess("""
                {
                  "rsp_code": "00000",
                  "account_cnt": 1,
                  "account_list": [{
                    "account_num": "1100101000001",
                    "is_consent": true,
                    "prod_name": "KB 입출금통장",
                    "account_type": "1001",
                    "institution_name": "KB국민은행",
                    "account_num_masked": "110-***-0001",
                    "balance_amt": 1800000
                  }]
                }
                """, MediaType.APPLICATION_JSON));

        List<MydataAccountData> result = client.getAccounts(101L);

        assertEquals(1, result.size());
        assertEquals("KB국민은행", result.get(0).getInstitutionName());
        assertEquals(1_800_000, result.get(0).getBalanceAmount());
        server.verify();
    }

    @Test
    void 카드_목록에서는_체크카드만_반환한다() {
        server.expect(requestTo("http://localhost:3000/v2/card/cards"))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer mock-access-token-user-8"))
            .andRespond(withSuccess("""
                {
                  "rsp_code": "00000",
                  "card_cnt": 2,
                  "card_list": [
                    {"card_id": "DEBIT-1", "card_type": "02", "card_name": "체크카드"},
                    {"card_id": "CREDIT-1", "card_type": "01", "card_name": "신용카드"}
                  ]
                }
                """, MediaType.APPLICATION_JSON));

        List<MydataCardData> result = client.getDebitCards(208L);

        assertEquals(1, result.size());
        assertEquals("DEBIT-1", result.get(0).getCardId());
        server.verify();
    }

    @Test
    void 선택한_계좌의_거래내역을_조회한다() {
        server.expect(requestTo(
                "http://localhost:3000/v2/bank/accounts/deposit/transactions"
            ))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer mock-access-token-user-1"))
            .andRespond(withSuccess("""
                {
                  "rsp_code": "00000",
                  "trans_cnt": 1,
                  "trans_list": [{
                    "trans_dtime": "20260801080000",
                    "trans_no": "108001",
                    "trans_type": "01",
                    "trans_amt": 2810000,
                    "trans_memo": "급여"
                  }]
                }
                """, MediaType.APPLICATION_JSON));

        List<MydataAccountTransactionData> result = client.getAccountTransactions(
            101L,
            "1100101000001"
        );

        assertEquals(1, result.size());
        assertEquals("108001", result.get(0).getTransactionNumber());
        server.verify();
    }

    @Test
    void 선택한_카드의_승인내역을_조회한다() {
        server.expect(requestTo(
                "http://localhost:3000/v2/card/cards/MOCK-CARD-01-01/approval-domestic"
                    + "?from_date=20260601&to_date=20260831"
            ))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer mock-access-token-user-1"))
            .andRespond(withSuccess("""
                {
                  "rsp_code": "00000",
                  "approved_cnt": 1,
                  "approved_list": [{
                    "approved_num": "108104",
                    "approved_dtime": "20260804123300",
                    "status": "01",
                    "merchant_name": "넷플릭스",
                    "merchant_regno": "900-00-00003",
                    "approved_amt": 17000,
                    "merchant_category_code": "4899"
                  }]
                }
                """, MediaType.APPLICATION_JSON));

        List<MydataCardApprovalData> result = client.getCardApprovals(
            101L,
            "MOCK-CARD-01-01"
        );

        assertEquals(1, result.size());
        assertEquals("넷플릭스", result.get(0).getMerchantName());
        server.verify();
    }
}
