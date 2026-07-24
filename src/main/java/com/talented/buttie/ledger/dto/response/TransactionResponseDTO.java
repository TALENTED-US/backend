package com.talented.buttie.ledger.dto.response;

import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.ledger.domain.TransactionType;
import com.talented.buttie.ledger.domain.TransactionVO;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record TransactionResponseDTO(
    String content,
    TransactionType transactionType,
    ExpenseCategory category,
    Integer amount,
    LocalDateTime transactionAt,
    String memo
) {

    public static TransactionResponseDTO from(TransactionVO vo) {
        return vo == null ? null : TransactionResponseDTO.builder()
            .content(vo.getContent())
            .transactionType(vo.getTransactionType())
            .category(vo.getCategory())
            .amount(vo.getAmount())
            .transactionAt(vo.getTransactionAt())
            .memo(vo.getMemo())
            .build();
    }
}