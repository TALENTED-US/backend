package com.talented.buttie.mydata.service;

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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public SyncResult sync(
        Long userId,
        MydataAssetRegistrationService.RegisteredAssets assets
    ) {
        int inserted = 0;
        int skipped = 0;

        for (AccountVO account : assets.accounts()) {
            List<MydataAccountTransactionData> transactions =
                mydataApiClient.getAccountTransactions(userId, account.getExternalAccountId());
            for (MydataAccountTransactionData source : transactions) {
                if (!isValidAccountTransaction(source)
                    || transactionMapper.existsByAccountAndExternalId(
                        account.getAccountId(),
                        source.getTransactionNumber()
                    )) {
                    skipped++;
                    continue;
                }
                insert(toAccountTransaction(userId, account, source));
                inserted++;
            }
        }

        for (CardVO card : assets.cards()) {
            List<MydataCardApprovalData> approvals = mydataApiClient.getCardApprovals(
                userId,
                card.getExternalCardId()
            );
            for (MydataCardApprovalData source : approvals) {
                if (!isValidCardApproval(source)
                    || transactionMapper.existsByCardAndExternalId(
                        card.getCardId(),
                        source.getApprovalNumber()
                    )) {
                    skipped++;
                    continue;
                }
                insert(toCardTransaction(userId, card, source));
                inserted++;
            }
        }

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

    private void insert(TransactionVO transaction) {
        if (transactionMapper.insertTransaction(transaction) != 1) {
            throw ApplicationException.from(MydataErrorCode.MYDATA_TRANSACTION_SYNC_FAILED);
        }
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
