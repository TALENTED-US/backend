package com.talented.buttie.ledger.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.dto.request.DeleteTransactionRequest;
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
    public Long deleteTransaction(Long userId, DeleteTransactionRequest request) {
        if (request == null || request.transactionId() == null || userId == null) {
            throw ApplicationException.from(LedgerErrorCode.TRANSACTION_BAD_REQUEST);
        }

        TransactionVO original = transactionMapper.findById(request.transactionId());

        if (original == null) {
            throw ApplicationException.from(LedgerErrorCode.TRANSACTION_NOT_FOUND);
        }

        if (!original.getUserId().equals(userId)) {
            throw ApplicationException.from(LedgerErrorCode.TRANSACTION_DELETE_USER_ID_MISMATCH);
        }

        boolean isExternal = original.getExternalTransactionId() != null
            && !original.getExternalTransactionId().isBlank();

        if (isExternal) {
            throw ApplicationException.from(LedgerErrorCode.EXTERNAL_TRANSACTION_UNDELETABLE);
        }

        transactionMapper.deleteTransaction(request.transactionId());

        return userId;
    }
}
