package com.talented.buttie.ledger.service.transaction;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.TransactionSource;
import com.talented.buttie.ledger.domain.TransactionType;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.dto.request.transaction.ClassifyAccountTransactionRequest;
import com.talented.buttie.ledger.exception.LedgerErrorCode;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClassifyAccountTransactionService {

    private final TransactionMapper transactionMapper;

    @Transactional
    public TransactionVO classify(
        Long userId,
        Long transactionId,
        ClassifyAccountTransactionRequest request
    ) {
        TransactionVO transaction = transactionMapper.findById(transactionId);

        if (transaction == null) {
            throw ApplicationException.from(LedgerErrorCode.TRANSACTION_NOT_FOUND);
        }
        if (!transaction.getUserId().equals(userId)) {
            throw ApplicationException.from(LedgerErrorCode.TRANSACTION_USER_ID_MISMATCH);
        }
        if (transaction.getTransactionSource() != TransactionSource.ACCOUNT
            || transaction.getTransactionType() != TransactionType.TRANSFER) {
            throw ApplicationException.from(LedgerErrorCode.ACCOUNT_TRANSFER_REQUIRED);
        }
        if (request.transactionType() != TransactionType.EXPENSE
            && request.transactionType() != TransactionType.FIXED) {
            throw ApplicationException.from(LedgerErrorCode.INVALID_EXPENSE_CLASSIFICATION_TYPE);
        }

        TransactionVO classified = TransactionVO.classifyAccountExpense(
            transaction,
            request.transactionType(),
            request.expenseCategory()
        );

        if (transactionMapper.updateTransactionClassification(classified) == 0) {
            throw ApplicationException.from(LedgerErrorCode.TRANSACTION_NOT_FOUND);
        }
        return classified;
    }
}
