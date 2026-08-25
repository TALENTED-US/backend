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
    void 외부_API_호출로_계좌입금과_카드승인을_각각_수입과_카드지출_후보로_변환한다() {
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
            .build();

        given(mydataApiClient.getAccountTransactions(101L, "ACCOUNT-1"))
            .willReturn(List.of(income));
        given(mydataApiClient.getCardApprovals(101L, "CARD-1"))
            .willReturn(List.of(cardExpense));
        given(merchantCategoryClassifier.classify(cardExpense)).willReturn(
            new MerchantCategoryClassifier.ClassificationResult(
                ExpenseCategory.HOBBY_LEISURE,
                ClassificationMethod.MERCHANT_REGNO
            )
        );

        List<TransactionVO> candidates = service.fetchExternalTransactions(
            101L,
            new MydataAssetRegistrationService.RegisteredAssets(List.of(account), List.of(card))
        );

        assertEquals(2, candidates.size());
        TransactionVO savedIncome = candidates.get(0);
        TransactionVO savedCardExpense = candidates.get(1);
        assertEquals(TransactionType.INCOME, savedIncome.getTransactionType());
        assertEquals(TransactionSource.ACCOUNT, savedIncome.getTransactionSource());
        assertEquals(TransactionType.FIXED, savedCardExpense.getTransactionType());
        assertEquals(ExpenseCategory.HOBBY_LEISURE, savedCardExpense.getExpenseCategory());
        assertEquals("900-00-00003", savedCardExpense.getMerchantRegistrationNumber());
    }

    @Test
    void 유효하지_않은_데이터는_후보에서_제외한다() {
        AccountVO account = AccountVO.builder()
            .accountId(11L)
            .externalAccountId("ACCOUNT-1")
            .build();
        MydataAccountTransactionData invalid = MydataAccountTransactionData.builder()
            .transactionNumber(null)
            .transactionType("01")
            .transactionAmount(1_000)
            .transactionDateTime("20260801080000")
            .build();

        given(mydataApiClient.getAccountTransactions(101L, "ACCOUNT-1"))
            .willReturn(List.of(invalid));

        List<TransactionVO> candidates = service.fetchExternalTransactions(
            101L,
            new MydataAssetRegistrationService.RegisteredAssets(List.of(account), List.of())
        );

        assertEquals(0, candidates.size());
    }

    @Test
    void 이미_저장된_거래는_건너뛰고_새_거래만_저장한다() {
        TransactionVO newTransaction = TransactionVO.builder()
            .userId(101L)
            .accountId(11L)
            .externalTransactionId("A-1")
            .transactionType(TransactionType.INCOME)
            .build();
        TransactionVO duplicateTransaction = TransactionVO.builder()
            .userId(101L)
            .accountId(11L)
            .externalTransactionId("A-2")
            .transactionType(TransactionType.INCOME)
            .build();

        List<TransactionVO> candidates = List.of(newTransaction, duplicateTransaction);
        // DB의 UNIQUE 제약(INSERT IGNORE)이 중복 1건을 걸러내 1건만 실제로 삽입됐다고 가정
        given(transactionMapper.insertTransactionsIgnoreDuplicates(candidates)).willReturn(1);

        MydataTransactionImportService.SyncResult result = service.importTransactions(candidates);

        verify(transactionMapper).insertTransactionsIgnoreDuplicates(candidates);
        assertEquals(1, result.insertedCount());
        assertEquals(1, result.skippedCount());
    }

    @Test
    void 후보가_비어있으면_DB를_호출하지_않는다() {
        MydataTransactionImportService.SyncResult result = service.importTransactions(List.of());

        assertEquals(0, result.insertedCount());
        assertEquals(0, result.skippedCount());
        verify(transactionMapper, org.mockito.Mockito.never())
            .insertTransactionsIgnoreDuplicates(any());
    }
}
