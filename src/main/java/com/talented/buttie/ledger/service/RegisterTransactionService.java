package com.talented.buttie.ledger.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.dto.request.RegisterTransactionRequestDTO;
import com.talented.buttie.ledger.exception.LedgerErrorCode;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterTransactionService {
    private final TransactionMapper transactionMapper;

    @Transactional
    public TransactionVO registerTransaction(Long userId, RegisterTransactionRequestDTO request){
        if(request == null){
            throw ApplicationException.from(LedgerErrorCode.TRANSACTION_BAD_REQUEST);
        }

        TransactionVO transaction = request.toVO(userId);

        int result = transactionMapper.insertTransaction(transaction);

        if(result == 0){
            throw ApplicationException.from(LedgerErrorCode.TRANSACTION_BAD_REQUEST);
        }

        return transaction;
    }
}
