package com.talented.buttie.mydata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.talented.buttie.ledger.domain.ClassificationMethod;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.ledger.domain.TransactionSource;
import com.talented.buttie.ledger.domain.TransactionType;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import com.talented.buttie.mydata.client.MydataApiClient;
import com.talented.buttie.mydata.client.dto.MydataAccountTransactionData;
import com.talented.buttie.mydata.client.dto.MydataCardApprovalData;
import com.talented.buttie.mydata.domain.AccountVO;
import com.talented.buttie.mydata.domain.CardVO;
import com.talented.buttie.mydata.service.mydata.MerchantCategoryClassifier;
import com.talented.buttie.mydata.service.mydata.MydataAssetRegistrationService;
import com.talented.buttie.mydata.service.mydata.MydataTransactionImportService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MydataTransactionImportServiceTest {

    @Mock
    private MydataApiClient mydataApiClient;
    @Mock
    private TransactionMapper transactionMapper;
    @Mock
    private MerchantCategoryClassifier merchantCategoryClassifier;
    @InjectMocks
    private MydataTransactionImportService service;

    @Test
    void 계좌입금과_카드승인을_각각_수입과_카드지출로_저장한다() {
        AccountVO account = AccountVO.builder()
            .accountId(11L)
            .externalAccountId("ACCOUNT-1")
            .build();
        CardVO card = CardVO.builder()
            .cardId(21L)
            .externalCardId("CARD-1")
            .build();
        MydataAccountTransactionData income = MydataAccountTransactionData.builder()
            .transactionNumber("A-1")
            .transactionType("01")
            .transactionAmount(2_800_000)
            .transactionDateTime("20260801080000")
            .transactionMemo("급여")
            .build();
        MydataCardApprovalData cardExpense = MydataCardApprovalData.builder()
            .approvalNumber("C-1")
            .status("01")
            .approvedAmount(17_000)
            .approvedDateTime("20260804123300")
            .merchantName("넷플릭스")
            .merchantRegistrationNumber("900-00-00003")
            .merchantCategoryCode("4899")
            .build();

        given(mydataApiClient.getAccountTransactions(101L, "ACCOUNT-1"))
            .willReturn(List.of(income));
        given(mydataApiClient.getCardApprovals(101L, "CARD-1"))
            .willReturn(List.of(cardExpense));
        given(merchantCategoryClassifier.classify(cardExpense)).willReturn(
            new MerchantCategoryClassifier.ClassificationResult(
                ExpenseCategory.SUBSCRIPTION,
                ClassificationMethod.MERCHANT_REGNO
            )
        );
        given(transactionMapper.insertTransaction(any(TransactionVO.class))).willReturn(1);

        MydataTransactionImportService.SyncResult result = service.sync(
            101L,
            new MydataAssetRegistrationService.RegisteredAssets(
                List.of(account),
                List.of(card)
            )
        );

        ArgumentCaptor<TransactionVO> captor = ArgumentCaptor.forClass(TransactionVO.class);
        verify(transactionMapper, org.mockito.Mockito.times(2))
            .insertTransaction(captor.capture());
        TransactionVO savedIncome = captor.getAllValues().get(0);
        TransactionVO savedCardExpense = captor.getAllValues().get(1);
        assertEquals(TransactionType.INCOME, savedIncome.getTransactionType());
        assertEquals(TransactionSource.ACCOUNT, savedIncome.getTransactionSource());
        assertEquals(TransactionType.EXPENSE, savedCardExpense.getTransactionType());
        assertEquals(ExpenseCategory.SUBSCRIPTION, savedCardExpense.getExpenseCategory());
        assertEquals("900-00-00003", savedCardExpense.getMerchantRegistrationNumber());
        assertEquals(2, result.insertedCount());
    }
}
