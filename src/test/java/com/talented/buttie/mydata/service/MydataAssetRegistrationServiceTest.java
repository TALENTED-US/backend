package com.talented.buttie.mydata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.talented.buttie.mydata.client.MydataApiClient;
import com.talented.buttie.mydata.client.dto.MydataAccountData;
import com.talented.buttie.mydata.client.dto.MydataCardData;
import com.talented.buttie.mydata.domain.AccountVO;
import com.talented.buttie.mydata.domain.CardVO;
import com.talented.buttie.mydata.dto.request.RegisterMydataAssetsRequest;
import com.talented.buttie.mydata.mapper.AccountMapper;
import com.talented.buttie.mydata.mapper.CardMapper;
import com.talented.buttie.mydata.service.account.AccountCreateService;
import com.talented.buttie.mydata.service.account.AccountUpdateService;
import com.talented.buttie.mydata.service.card.CardCreateService;
import com.talented.buttie.mydata.service.card.CardUpdateService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MydataAssetRegistrationServiceTest {

    @Mock
    private MydataApiClient mydataApiClient;
    @Mock
    private MydataConnectionValidator mydataConnectionValidator;
    @Mock
    private AccountMapper accountMapper;
    @Mock
    private CardMapper cardMapper;
    @Mock
    private AccountCreateService accountCreateService;
    @Mock
    private AccountUpdateService accountUpdateService;
    @Mock
    private CardCreateService cardCreateService;
    @Mock
    private CardUpdateService cardUpdateService;
    @InjectMocks
    private MydataAssetRegistrationService service;

    @Test
    void 사용자가_선택한_계좌와_체크카드만_등록한다() {
        MydataAccountData account = MydataAccountData.builder()
            .accountNum("ACCOUNT-1")
            .isConsent(true)
            .productName("생활비 통장")
            .accountType("1001")
            .institutionName("KB국민은행")
            .build();
        MydataCardData card = MydataCardData.builder()
            .cardId("CARD-1")
            .isConsent(true)
            .cardName("체크카드")
            .cardType("02")
            .institutionName("KB국민카드")
            .build();
        given(mydataApiClient.getAccounts(101L)).willReturn(List.of(account));
        given(mydataApiClient.getDebitCards(101L)).willReturn(List.of(card));
        given(accountCreateService.create(any(AccountVO.class)))
            .willAnswer(invocation -> invocation.getArgument(0));
        given(cardCreateService.create(any(CardVO.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        MydataAssetRegistrationService.RegisteredAssets result =
            service.registerSelectedAssets(
                101L,
                new RegisterMydataAssetsRequest(List.of("ACCOUNT-1"), List.of("CARD-1"))
            );

        assertEquals(1, result.accounts().size());
        assertEquals(1, result.cards().size());
        verify(mydataConnectionValidator).validateConnected(101L);
        verify(accountMapper).deactivateAllByUserId(101L);
        verify(cardMapper).deactivateAllByUserId(101L);
        verify(accountCreateService).create(any(AccountVO.class));
        verify(cardCreateService).create(any(CardVO.class));
    }
}
