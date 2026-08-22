package com.talented.buttie.mydata.service.mydata;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.ClassificationMethod;
import com.talented.buttie.ledger.domain.TransactionSource;
import com.talented.buttie.ledger.domain.TransactionType;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import com.talented.buttie.mydata.client.MydataApiClient;
import com.talented.buttie.mydata.client.dto.MydataAccountTransactionData;
import com.talented.buttie.mydata.client.dto.MydataCardApprovalData;
import com.talented.buttie.mydata.domain.AccountVO;
import com.talented.buttie.mydata.domain.CardVO;
import com.talented.buttie.mydata.exception.MydataErrorCode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MydataTransactionImportService {

    private static final String DEPOSIT_TYPE = "01";
    private static final String APPROVED_STATUS = "01";
    private static final DateTimeFormatter MYDATA_DATE_TIME = DateTimeFormatter.ofPattern(
        "yyyyMMddHHmmss"
    );

    private final MydataApiClient mydataApiClient;
    private final TransactionMapper transactionMapper;
    private final MerchantCategoryClassifier merchantCategoryClassifier;

    // 외부 마이데이터 API만 호출. DB 커넥션을 사용하지 않음
    public List<TransactionVO> fetchExternalTransactions(
        Long userId,
        MydataAssetRegistrationService.RegisteredAssets assets
    ) {
        List<TransactionVO> candidates = new ArrayList<>();

        for(AccountVO account: assets.accounts()) {
            List<MydataAccountTransactionData> transactions =
                mydataApiClient.getAccountTransactions(userId, account.getExternalAccountId());
            for(MydataAccountTransactionData source: transactions) {
                if(isValidAccountTransaction(source)) {
                    candidates.add(toAccountTransaction(userId, account, source));
                }
            }
        }

        for(CardVO card: assets.cards()) {
            List<MydataCardApprovalData> approvals =
                mydataApiClient.getCardApprovals(userId, card.getExternalCardId());
            for(MydataCardApprovalData source: approvals) {
                if(isValidCardApproval(source)) {
                    candidates.add(toCardTransaction(userId, card, source));
                }
            }
        }

        return candidates;
    }

    // 배치 insert 1회로 dedup + insert를 수행. DB 커넥션을 짧게 점유함.
    // (ACCOUNT_ID, EXTERNAL_TRANSACTION_ID), (CARD_ID, EXTERNAL_TRANSACTION_ID) UNIQUE 제약을
    // 활용해 INSERT IGNORE로 중복은 DB가 걸러내고, 건당 존재 여부 SELECT 왕복을 없앤다.
    @Transactional
    public SyncResult importTransactions(List<TransactionVO> candidates) {
        if (candidates.isEmpty()) {
            return new SyncResult(0, 0);
        }

        int inserted = transactionMapper.insertTransactionsIgnoreDuplicates(candidates);
        int skipped = candidates.size() - inserted;

        return new SyncResult(inserted, skipped);
    }

    private TransactionVO toAccountTransaction(
        Long userId,
        AccountVO account,
        MydataAccountTransactionData source
    ) {
        boolean income = DEPOSIT_TYPE.equals(source.getTransactionType());
        return TransactionVO.builder()
            .userId(userId)
            .accountId(account.getAccountId())
            .externalTransactionId(source.getTransactionNumber())
            .transactionSource(TransactionSource.ACCOUNT)
            .classificationMethod(
                income ? ClassificationMethod.ACCOUNT_INFLOW : ClassificationMethod.UNCLASSIFIED
            )
            .transactionContent(defaultText(source.getTransactionMemo(), "계좌 거래"))
            .transactionType(income ? TransactionType.INCOME : TransactionType.TRANSFER)
            .expenseCategory(null)
            .transactionAmount(source.getTransactionAmount())
            .transactionAt(parseDateTime(source.getTransactionDateTime()))
            .transactionMemo(source.getTransactionMemo())
            .analysisExcluded(false)
            .build();
    }

    private TransactionVO toCardTransaction(
        Long userId,
        CardVO card,
        MydataCardApprovalData source
    ) {
        MerchantCategoryClassifier.ClassificationResult classification =
            merchantCategoryClassifier.classify(source);
        return TransactionVO.builder()
            .userId(userId)
            .cardId(card.getCardId())
            .externalTransactionId(source.getApprovalNumber())
            .transactionSource(TransactionSource.CARD)
            .classificationMethod(classification.method())
            .merchantName(source.getMerchantName())
            .merchantRegistrationNumber(source.getMerchantRegistrationNumber())
            .merchantCategoryCode(source.getMerchantCategoryCode())
            .transactionContent(defaultText(source.getMerchantName(), "카드 결제"))
            .transactionType(TransactionType.EXPENSE)
            .expenseCategory(classification.category())
            .transactionAmount(source.getApprovedAmount())
            .transactionAt(parseDateTime(source.getApprovedDateTime()))
            .transactionMemo(source.getMerchantCategoryName())
            .analysisExcluded(false)
            .build();
    }

    private boolean isValidAccountTransaction(MydataAccountTransactionData source) {
        return source != null
            && source.getTransactionNumber() != null
            && source.getTransactionDateTime() != null
            && source.getTransactionAmount() != null;
    }

    private boolean isValidCardApproval(MydataCardApprovalData source) {
        return source != null
            && APPROVED_STATUS.equals(source.getStatus())
            && source.getApprovalNumber() != null
            && source.getApprovedDateTime() != null
            && source.getApprovedAmount() != null;
    }

    private LocalDateTime parseDateTime(String value) {
        try {
            return LocalDateTime.parse(value, MYDATA_DATE_TIME);
        } catch (DateTimeParseException exception) {
            throw ApplicationException.from(MydataErrorCode.MYDATA_TRANSACTION_SYNC_FAILED);
        }
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    public record SyncResult(int insertedCount, int skippedCount) {

    }
}
