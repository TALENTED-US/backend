package com.talented.buttie.ledger.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.exception.LedgerErrorCode;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetSumFixedExpenseService {

    private final TransactionMapper transactionMapper;

    @Transactional(readOnly = true)
    public List<TransactionVO> getFixedExpenses(Long userId) {
        List<TransactionVO> fixedExpenses = transactionMapper.findFixedExpensesByUserId(userId);

        if (fixedExpenses == null || fixedExpenses.isEmpty()) {
            throw ApplicationException.from(LedgerErrorCode.FIXED_EXPENSE_NOT_FOUND);
        }

        return fixedExpenses;
    }
}
