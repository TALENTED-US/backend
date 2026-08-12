package com.talented.buttie.mydata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

import com.talented.buttie.mydata.client.MydataApiClient;
import com.talented.buttie.mydata.client.dto.MydataAccountData;
import com.talented.buttie.mydata.client.dto.MydataCardData;
import com.talented.buttie.mydata.dto.response.MydataAssetsResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MydataAssetQueryServiceTest {

    @Mock
    private MydataApiClient mydataApiClient;

    @InjectMocks
    private MydataAssetQueryService mydataAssetQueryService;

    @Test
    void 사용자별_계좌와_체크카드를_함께_조회한다() {
        given(mydataApiClient.getAccounts(101L)).willReturn(List.of(
            MydataAccountData.builder()
                .accountNum("110101000001")
                .institutionName("KB국민은행")
                .productName("KB국민은행 입출금")
                .accountNumberMasked("***-***-2847")
                .balanceAmount(1_800_000)
                .isConsent(true)
                .build()
        ));
        given(mydataApiClient.getDebitCards(101L)).willReturn(List.of(
            MydataCardData.builder()
                .cardId("CARD-01-01")
                .institutionName("KB국민카드")
                .cardName("KB 데일리 체크카드")
                .cardNumberMasked("5412******3010")
                .cardType("02")
                .isConsent(true)
                .build()
        ));

        MydataAssetsResponse result = mydataAssetQueryService.getAssets(101L);

        assertEquals(1, result.accounts().size());
        assertEquals(1_800_000, result.accounts().get(0).balance());
        assertEquals("02", result.cards().get(0).cardType());
    }
}
