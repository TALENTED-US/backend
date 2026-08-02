package com.talented.buttie.ledger.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.exception.LedgerErrorCode;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.dto.request.UpdateTransactionMemoRequestDTO;
import com.talented.buttie.ledger.dto.request.UpdateTransactionRequestDTO;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateTransactionService {

    private final TransactionMapper transactionMapper;

    /**
     * 수동 등록 거래 전체 수정 (외부거래 ID가 없을 때 사용)
     * UpdateTransactionRequestDTO 수신
     * 외부거래 ID가 존재하는 경우 EXTERNAL_TRANSACTION_UNMODIFIABLE 예외 발생
     */
    @Transactional
    public TransactionVO updateTransaction(Long userId, Long transactionId, UpdateTransactionRequestDTO request) {
        TransactionVO original = transactionMapper.findById(transactionId);
        if (original == null || !original.getUserId().equals(userId)) {
            throw ApplicationException.from(LedgerErrorCode.TRANSACTION_NOT_FOUND);
        }

        boolean isExternal = original.getExternalTransactionId() != null
            && !original.getExternalTransactionId().isBlank();

        // 외부 연동 거래인 경우 전체 수정 불가 예외 던짐
        if (isExternal) {
            throw ApplicationException.from(LedgerErrorCode.EXTERNAL_TRANSACTION_UNMODIFIABLE);
        }

        LocalDateTime newTransactionAt = request.transactionDate() != null
            ? request.transactionDate()
            : original.getTransactionAt();

        TransactionVO updated = original.toBuilder()
            .transactionAmount(request.transactionAmount() != null ? request.transactionAmount() : original.getTransactionAmount())
            .expenseCategory(request.expenseCategory() != null ? request.expenseCategory() : original.getExpenseCategory())
            .transactionAt(newTransactionAt)
            .transactionMemo(request.transactionMemo())
            .build();

        transactionMapper.updateTransaction(updated);
        return updated;
    }

    /**
     * 외부 연동 거래 메모 단일 수정 (외부거래 ID가 있을 때 사용)
     * UpdateTransactionMemoRequestDTO 수신
     */
    @Transactional
    public TransactionVO updateTransactionMemo(Long userId, Long transactionId, UpdateTransactionMemoRequestDTO request) {
        TransactionVO original = transactionMapper.findById(transactionId);
        if (original == null || !original.getUserId().equals(userId)) {
            throw ApplicationException.from(LedgerErrorCode.TRANSACTION_NOT_FOUND);
        }

        String newMemo = request != null ? request.memo() : null;

        transactionMapper.updateTransactionMemo(transactionId, newMemo);

        return original.toBuilder()
            .transactionMemo(newMemo)
            .build();
    }
}