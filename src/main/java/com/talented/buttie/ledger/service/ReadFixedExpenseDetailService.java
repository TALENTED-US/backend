package com.talented.buttie.ledger.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.exception.LedgerErrorCode;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReadFixedExpenseDetailService {

    private final TransactionMapper transactionMapper;

    public List<TransactionVO> getFixedExpenseDetails(Long userId) {
        List<TransactionVO> transactions = transactionMapper.findFixedExpensesByUserId(userId);

        if (transactions == null || transactions.isEmpty()) {
            throw ApplicationException.from(LedgerErrorCode.FIXED_EXPENSE_NOT_FOUND);
        }

        return transactions;
    }
}
