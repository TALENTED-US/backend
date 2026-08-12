package com.talented.buttie.mydata.service;

import com.talented.buttie.ledger.domain.TransactionSource;
import com.talented.buttie.ledger.domain.TransactionType;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MydataDuplicateTransactionService {

    private static final long MAX_TIME_DIFFERENCE_MINUTES = 15L;

    private final TransactionMapper transactionMapper;

    public int excludeLikelyAccountDuplicates(Long userId) {
        List<TransactionVO> transactions = transactionMapper
            .findExternalTransactionsForAnalysis(userId);
        List<TransactionVO> cardExpenses = transactions.stream()
            .filter(transaction -> transaction.getTransactionSource() == TransactionSource.CARD)
            .filter(transaction -> transaction.getTransactionType() == TransactionType.EXPENSE)
            .collect(Collectors.toList());

        int excluded = 0;
        for (TransactionVO accountTransaction : transactions) {
            if (accountTransaction.getTransactionSource() != TransactionSource.ACCOUNT
                || accountTransaction.getTransactionType() != TransactionType.TRANSFER
                || Boolean.TRUE.equals(accountTransaction.getAnalysisExcluded())) {
                continue;
            }

            boolean duplicated = cardExpenses.stream()
                .anyMatch(cardTransaction -> isLikelyDuplicate(
                    accountTransaction,
                    cardTransaction
                ));
            boolean analysisExcluded = transactionMapper.updateAnalysisExcluded(
                accountTransaction.getTransactionId(),
                true
            ) == 1;
            if (duplicated && analysisExcluded) {
                excluded++;
            }
        }
        return excluded;
    }

    private boolean isLikelyDuplicate(
        TransactionVO accountTransaction,
        TransactionVO cardTransaction
    ) {
        if (accountTransaction.getTransactionAmount() == null
            || !accountTransaction.getTransactionAmount().equals(
                cardTransaction.getTransactionAmount()
            )
            || accountTransaction.getTransactionAt() == null
            || cardTransaction.getTransactionAt() == null) {
            return false;
        }

        long minutes = Math.abs(Duration.between(
            accountTransaction.getTransactionAt(),
            cardTransaction.getTransactionAt()
        ).toMinutes());
        if (minutes > MAX_TIME_DIFFERENCE_MINUTES) {
            return false;
        }

        String accountDescription = normalize(
            accountTransaction.getTransactionMemo() == null
                ? accountTransaction.getTransactionContent()
                : accountTransaction.getTransactionMemo()
        );
        String merchantName = normalize(cardTransaction.getMerchantName());
        return accountDescription.contains("카드")
            || (!merchantName.isEmpty() && accountDescription.contains(merchantName));
    }

    private String normalize(String value) {
        return value == null
            ? ""
            : value.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }
}
