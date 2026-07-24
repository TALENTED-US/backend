package com.talented.buttie.ledger.service;

import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.dto.response.TransactionResponseDTO;
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

        return transactions.stream().map(TransactionResponseDTO::from).toList();
    }
}
