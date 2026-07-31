package com.talented.buttie.ledger.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.exception.LedgerErrorCode;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.dto.request.UpdateTransactionRequestDTO;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateTransactionService {

    private final TransactionMapper transactionMapper;

    @Transactional
    public TransactionVO updateTransaction(Long userId, Long transactionId, UpdateTransactionRequestDTO request) {
        TransactionVO originalTransaction = transactionMapper.findById(transactionId);

        if (originalTransaction == null || !originalTransaction.getUserId().equals(userId)) {
            throw ApplicationException.from(LedgerErrorCode.TRANSACTION_NOT_FOUND);
        }

        boolean isExternal = originalTransaction.getExternalTransactionId() != null
            && !originalTransaction.getExternalTransactionId().isBlank();
        if (isExternal) {
            throw ApplicationException.from(LedgerErrorCode.EXTERNAL_TRANSACTION_UNMODIFIABLE);
        }

        TransactionVO updatedTransaction = originalTransaction.toBuilder()
            .transactionAmount(request.transactionAmount())
            .expenseCategory(request.expenseCategory())
            .transactionAt(request.transactionDate())
            .transactionMemo(request.transactionMemo())
            .build();

        transactionMapper.updateTransaction(updatedTransaction);
        return updatedTransaction;
    }

    @Transactional
    public TransactionVO updateMemo(Long userId, Long transactionId, String newMemo) {
        TransactionVO originalTransaction = transactionMapper.findById(transactionId);

        if (originalTransaction == null || !originalTransaction.getUserId().equals(userId)) {
            throw ApplicationException.from(LedgerErrorCode.TRANSACTION_NOT_FOUND);
        }

        TransactionVO updatedTransaction = originalTransaction.toBuilder()
            .transactionMemo(newMemo)
            .build();

        transactionMapper.updateTransaction(updatedTransaction);
        return updatedTransaction;
    }
}