package com.talented.buttie.ledger.service.transaction;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.dto.request.transaction.CreateTransactionRequest;
import com.talented.buttie.ledger.exception.LedgerErrorCode;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateTransactionService {
    private final TransactionMapper transactionMapper;

    @Transactional
    public Long createTransaction(Long userId, CreateTransactionRequest request){
        TransactionVO transaction = TransactionVO.createTransaction(userId, request);

        int result = transactionMapper.insertTransaction(transaction);

        if(result == 0){
            throw ApplicationException.from(LedgerErrorCode.TRANSACTION_BAD_REQUEST);
        }

        return transaction.getTransactionId();
    }
}
