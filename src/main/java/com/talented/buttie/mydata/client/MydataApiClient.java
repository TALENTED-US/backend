package com.talented.buttie.mydata.client;

import com.talented.buttie.mydata.client.dto.MydataAccountData;
import com.talented.buttie.mydata.client.dto.MydataAccountTransactionData;
import com.talented.buttie.mydata.client.dto.MydataAuthorizationResponse;
import com.talented.buttie.mydata.client.dto.MydataCardApprovalData;
import com.talented.buttie.mydata.client.dto.MydataCardData;
import com.talented.buttie.mydata.client.dto.MydataTokenResponse;
import java.util.List;

public interface MydataApiClient {

    MydataAuthorizationResponse authorize(Long userId);

    MydataTokenResponse issueToken(String authorizationCode);

    List<MydataAccountData> getAccounts(Long userId);

    List<MydataCardData> getDebitCards(Long userId);

    List<MydataAccountTransactionData> getAccountTransactions(
        Long userId,
        String externalAccountId
    );

    List<MydataCardApprovalData> getCardApprovals(Long userId, String externalCardId);
}
