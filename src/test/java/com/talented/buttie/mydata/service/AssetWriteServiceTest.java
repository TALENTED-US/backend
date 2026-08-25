package com.talented.buttie.mydata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.mydata.domain.AccountVO;
import com.talented.buttie.mydata.domain.CardVO;
import com.talented.buttie.mydata.exception.AccountErrorCode;
import com.talented.buttie.mydata.exception.CardErrorCode;
import com.talented.buttie.mydata.mapper.AccountMapper;
import com.talented.buttie.mydata.mapper.CardMapper;
import com.talented.buttie.mydata.service.account.AccountCreateService;
import com.talented.buttie.mydata.service.account.AccountUpdateService;
import com.talented.buttie.mydata.service.card.CardCreateService;
import com.talented.buttie.mydata.service.card.CardUpdateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssetWriteServiceTest {

    @Mock
    private AccountMapper accountMapper;
    @Mock
    private CardMapper cardMapper;

    private AccountCreateService accountCreateService;
    private AccountUpdateService accountUpdateService;
    private CardCreateService cardCreateService;
    private CardUpdateService cardUpdateService;

    @BeforeEach
    void setUp() {
        accountCreateService = new AccountCreateService(accountMapper);
        accountUpdateService = new AccountUpdateService(accountMapper);
        cardCreateService = new CardCreateService(cardMapper);
        cardUpdateService = new CardUpdateService(cardMapper);
    }

    @Test
    void 계좌와_카드를_각각_등록한다() {
        AccountVO account = AccountVO.builder().externalAccountId("ACCOUNT-1").build();
        CardVO card = CardVO.builder().externalCardId("CARD-1").build();
        given(accountMapper.insert(account)).willReturn(1);
        given(cardMapper.insert(card)).willReturn(1);

        assertSame(account, accountCreateService.create(account));
        assertSame(card, cardCreateService.create(card));
    }

    @Test
    void 계좌와_카드를_각각_수정한다() {
        AccountVO account = AccountVO.builder().externalAccountId("ACCOUNT-1").build();
        CardVO card = CardVO.builder().externalCardId("CARD-1").build();
        given(accountMapper.update(account)).willReturn(1);
        given(cardMapper.update(card)).willReturn(1);

        assertSame(account, accountUpdateService.update(account));
        assertSame(card, cardUpdateService.update(card));
    }

    @Test
    void 수정할_자산이_없으면_도메인_오류를_반환한다() {
        AccountVO account = AccountVO.builder().externalAccountId("MISSING-ACCOUNT").build();
        CardVO card = CardVO.builder().externalCardId("MISSING-CARD").build();
        given(accountMapper.update(account)).willReturn(0);
        given(cardMapper.update(card)).willReturn(0);

        ApplicationException accountException = assertThrows(
            ApplicationException.class,
            () -> accountUpdateService.update(account)
        );
        ApplicationException cardException = assertThrows(
            ApplicationException.class,
            () -> cardUpdateService.update(card)
        );

        assertEquals(AccountErrorCode.ACCOUNT_UPDATE_FAILED, accountException.getCode());
        assertEquals(CardErrorCode.CARD_UPDATE_FAILED, cardException.getCode());
    }
}
