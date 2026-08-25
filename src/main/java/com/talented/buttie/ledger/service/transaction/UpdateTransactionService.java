package com.talented.buttie.ledger.service.transaction;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.exception.LedgerErrorCode;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.dto.request.transaction.UpdateTransactionMemoRequest;
import com.talented.buttie.ledger.dto.request.transaction.UpdateTransactionRequest;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateTransactionService {

    private final TransactionMapper transactionMapper;

    @Transactional
    public TransactionVO updateTransaction(Long userId, Long transactionId, UpdateTransactionRequest request) {
        TransactionVO original = transactionMapper.findById(transactionId);

        if(original == null){
            throw ApplicationException.from(LedgerErrorCode.TRANSACTION_NOT_FOUND);
        }

        if (!original.getUserId().equals(userId)) {
            throw ApplicationException.from(LedgerErrorCode.TRANSACTION_USER_ID_MISMATCH);
        }

        boolean isExternal = original.getExternalTransactionId() != null
            && !original.getExternalTransactionId().isBlank();

        // 외부 연동 거래인 경우 전체 수정 불가 예외 던짐
        if (isExternal) {
            throw ApplicationException.from(LedgerErrorCode.EXTERNAL_TRANSACTION_UNMODIFIABLE);
        }

        TransactionVO updated = TransactionVO.updateTransaction(original, request);

        transactionMapper.updateTransaction(updated);
        return updated;
    }


    @Transactional
    public TransactionVO updateTransactionMemo(Long userId, Long transactionId, UpdateTransactionMemoRequest request) {
        TransactionVO original = transactionMapper.findById(transactionId);

        if(original == null){
            throw ApplicationException.from(LedgerErrorCode.TRANSACTION_NOT_FOUND);
        }
        if (!original.getUserId().equals(userId)) {
            throw ApplicationException.from(LedgerErrorCode.TRANSACTION_MEMO_USER_ID_MISMATCH);
        }

        transactionMapper.updateTransactionMemo(transactionId, request.memo());

        return TransactionVO.updateTransactionMemo(original, request);
    }
}