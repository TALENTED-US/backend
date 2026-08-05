package com.talented.buttie.ledger.service.fixed;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.TransactionType;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.exception.LedgerErrorCode;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteFixedExpenseService {

    private final TransactionMapper transactionMapper;

    @Transactional
    public Long deleteFixedExpense(Long userId, Long transactionId) {
        TransactionVO targetTransaction = transactionMapper.findById(transactionId);

        if (targetTransaction == null) {
            throw ApplicationException.from(LedgerErrorCode.TRANSACTION_NOT_FOUND);
        }

        if (!targetTransaction.getUserId().equals(userId)) {
            throw ApplicationException.from(LedgerErrorCode.TRANSACTION_USER_ID_MISMATCH);
        }

        if (targetTransaction.getTransactionType() != TransactionType.FIXED) {
            throw ApplicationException.from(LedgerErrorCode.NOT_FIXED_EXPENSE);
        }

        TransactionVO updatedTransaction = TransactionVO.deleteFixedExpense(targetTransaction);
        int updatedRows = transactionMapper.updateTransaction(updatedTransaction);

        if (updatedRows == 0) {
            throw ApplicationException.from(LedgerErrorCode.TRANSACTION_NOT_FOUND);
        }

        return updatedTransaction.getTransactionId();
    }
}
