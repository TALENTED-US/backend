package com.talented.buttie.mydata.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.mydata.client.MydataApiClient;
import com.talented.buttie.mydata.client.dto.MydataAccountData;
import com.talented.buttie.mydata.client.dto.MydataCardData;
import com.talented.buttie.mydata.domain.AccountType;
import com.talented.buttie.mydata.domain.AccountVO;
import com.talented.buttie.mydata.domain.CardType;
import com.talented.buttie.mydata.domain.CardVO;
import com.talented.buttie.mydata.domain.ConnectionStatus;
import com.talented.buttie.mydata.domain.MydataConnectionVO;
import com.talented.buttie.mydata.dto.request.RegisterMydataAssetsRequest;
import com.talented.buttie.mydata.exception.MydataErrorCode;
import com.talented.buttie.mydata.mapper.AccountMapper;
import com.talented.buttie.mydata.mapper.CardMapper;
import com.talented.buttie.mydata.mapper.MydataConnectionMapper;
import com.talented.buttie.mydata.service.account.AccountCreateService;
import com.talented.buttie.mydata.service.account.AccountUpdateService;
import com.talented.buttie.mydata.service.card.CardCreateService;
import com.talented.buttie.mydata.service.card.CardUpdateService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MydataAssetRegistrationService {

    private static final String MOCK_PROVIDER = "MOCK";

    private final MydataApiClient mydataApiClient;
    private final MydataConnectionMapper mydataConnectionMapper;
    private final AccountMapper accountMapper;
    private final CardMapper cardMapper;
    private final AccountCreateService accountCreateService;
    private final AccountUpdateService accountUpdateService;
    private final CardCreateService cardCreateService;
    private final CardUpdateService cardUpdateService;

    @Transactional
    public RegisteredAssets registerSelectedAssets(
        Long userId,
        RegisterMydataAssetsRequest request
    ) {
        validateConnected(userId);

        Set<String> selectedAccountIds = new LinkedHashSet<>(request.accountIds());
        Set<String> selectedCardIds = new LinkedHashSet<>(request.cardIds());
        Map<String, MydataAccountData> availableAccounts = mydataApiClient.getAccounts(userId)
            .stream()
            .filter(account -> Boolean.TRUE.equals(account.getIsConsent()))
            .collect(Collectors.toMap(
                MydataAccountData::getAccountNum,
                Function.identity()
            ));
        Map<String, MydataCardData> availableCards = mydataApiClient.getDebitCards(userId)
            .stream()
            .filter(card -> Boolean.TRUE.equals(card.getIsConsent()))
            .collect(Collectors.toMap(MydataCardData::getCardId, Function.identity()));

        if (!availableAccounts.keySet().containsAll(selectedAccountIds)
            || !availableCards.keySet().containsAll(selectedCardIds)) {
            throw ApplicationException.from(MydataErrorCode.MYDATA_ASSET_SELECTION_INVALID);
        }

        accountMapper.deactivateAllByUserId(userId);
        cardMapper.deactivateAllByUserId(userId);

        List<AccountVO> accounts = new ArrayList<>();
        for (String externalAccountId : selectedAccountIds) {
            AccountVO account = toAccount(userId, availableAccounts.get(externalAccountId));
            AccountVO existing = accountMapper.findByUserIdAndExternalId(
                userId,
                externalAccountId
            );
            if (existing == null) {
                accountCreateService.create(account);
            } else {
                account.setAccountId(existing.getAccountId());
                accountUpdateService.update(account);
            }
            accounts.add(account);
        }

        List<CardVO> cards = new ArrayList<>();
        for (String externalCardId : selectedCardIds) {
            CardVO card = toCard(userId, availableCards.get(externalCardId));
            CardVO existing = cardMapper.findByUserIdAndExternalId(userId, externalCardId);
            if (existing == null) {
                cardCreateService.create(card);
            } else {
                card.setCardId(existing.getCardId());
                card.setLinkedAccountId(existing.getLinkedAccountId());
                card.setLinkedBankCode(existing.getLinkedBankCode());
                cardUpdateService.update(card);
            }
            cards.add(card);
        }

        return new RegisteredAssets(accounts, cards);
    }

    private void validateConnected(Long userId) {
        MydataConnectionVO connection = mydataConnectionMapper.findByUserIdAndProvider(
            userId,
            MOCK_PROVIDER
        );
        if (connection == null || connection.getMydataStatus() != ConnectionStatus.CONNECTED) {
            throw ApplicationException.from(MydataErrorCode.MYDATA_NOT_CONNECTED);
        }
    }

    private AccountVO toAccount(Long userId, MydataAccountData source) {
        return AccountVO.builder()
            .userId(userId)
            .externalAccountId(source.getAccountNum())
            .institutionName(source.getInstitutionName())
            .accountName(source.getProductName())
            .accountType(toAccountType(source.getAccountType()))
            .accountNumberMasked(source.getAccountNumberMasked())
            .balance(source.getBalanceAmount() == null ? 0 : source.getBalanceAmount())
            .isActive(true)
            .syncedAt(LocalDateTime.now())
            .build();
    }

    private CardVO toCard(Long userId, MydataCardData source) {
        return CardVO.builder()
            .userId(userId)
            .externalCardId(source.getCardId())
            .cardInstitutionName(source.getInstitutionName())
            .cardName(source.getCardName())
            .cardType(CardType.DEBIT)
            .cardNumberMasked(source.getCardNumberMasked())
            .cardBalance(0)
            .cardIsActive(true)
            .cardSyncedAt(LocalDateTime.now())
            .build();
    }

    private AccountType toAccountType(String accountTypeCode) {
        if (accountTypeCode == null) {
            return AccountType.CHECKING;
        }
        return switch (accountTypeCode) {
            case "2001", "2002" -> AccountType.SAVINGS;
            case "3001", "3002" -> AccountType.LOAN;
            default -> AccountType.CHECKING;
        };
    }

    public record RegisteredAssets(List<AccountVO> accounts, List<CardVO> cards) {
    }
}
