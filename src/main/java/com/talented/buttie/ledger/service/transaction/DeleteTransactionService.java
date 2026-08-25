package com.talented.buttie.ledger.service.transaction;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.exception.LedgerErrorCode;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteTransactionService {

    private final TransactionMapper transactionMapper;

    @Transactional
    public Long deleteTransaction(Long userId, Long transactionId) {

        TransactionVO targetTransaction = transactionMapper.findById(transactionId);

        if (targetTransaction == null) {
            throw ApplicationException.from(LedgerErrorCode.TRANSACTION_NOT_FOUND);
        }

        if (!targetTransaction.getUserId().equals(userId)) {
            throw ApplicationException.from(LedgerErrorCode.TRANSACTION_DELETE_USER_ID_MISMATCH);
        }

        boolean isExternal = targetTransaction.getExternalTransactionId() != null
            && !targetTransaction.getExternalTransactionId().isBlank();

        if (isExternal) {
            throw ApplicationException.from(LedgerErrorCode.EXTERNAL_TRANSACTION_UNDELETABLE);
        }

        transactionMapper.deleteTransaction(transactionId);

        return userId;
    }
}
