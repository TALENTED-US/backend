package com.talented.buttie.ledger.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.dto.response.TransactionResponseDTO;
import com.talented.buttie.ledger.exception.LedgerErrorCode;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetTransactionService {
    private final TransactionMapper transactionMapper;

    public List<TransactionResponseDTO> getAllTransactions(Long userId){
        List<TransactionVO> transactions = transactionMapper.findAllByUserId(userId);

        if (transactions == null || transactions.isEmpty()) {
            throw ApplicationException.from(LedgerErrorCode.TRANSACTION_NOT_FOUND);
        }
        else {
            Long ownerId = transactions.get(0).getUserId();
            if(!userId.equals(ownerId)){
                throw ApplicationException.from(LedgerErrorCode.TRANSACTION_USER_ID_MISMATCH);
            }
        }

        return transactions.stream().map(TransactionResponseDTO::from).toList();
    }
}
