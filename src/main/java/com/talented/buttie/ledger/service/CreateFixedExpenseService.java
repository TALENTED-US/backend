package com.talented.buttie.ledger.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.TransactionType;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.dto.request.CreateFixedExpenseRequestDTO;
import com.talented.buttie.ledger.exception.LedgerErrorCode;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateFixedExpenseService {

    private final TransactionMapper transactionMapper;

    @Transactional
    public Long createFixedExpense(Long userId, CreateFixedExpenseRequestDTO request) {
        TransactionVO original = transactionMapper.findById(request.transactionId());

        if (original == null) {
            throw ApplicationException.from(LedgerErrorCode.TRANSACTION_NOT_FOUND);
        }

        if (!original.getUserId().equals(userId)) {
            throw ApplicationException.from(LedgerErrorCode.TRANSACTION_USER_ID_MISMATCH);
        }

        if (original.getTransactionType() == TransactionType.FIXED) {
            throw ApplicationException.from(LedgerErrorCode.ALREADY_FIXED_EXPENSE);
        }

        TransactionVO fixedExpense = TransactionVO.createFixedExpense(original);
        transactionMapper.updateTransaction(fixedExpense);

        return fixedExpense.getTransactionId();
    }
}
