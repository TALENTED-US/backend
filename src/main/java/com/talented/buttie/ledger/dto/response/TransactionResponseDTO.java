package com.talented.buttie.ledger.dto.response;

import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.ledger.domain.TransactionType;
import com.talented.buttie.ledger.domain.TransactionVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@ApiModel(description = "거래 목록 조회 응답")
@Builder
public record TransactionResponseDTO(
    @ApiModelProperty(value = "거래 내용", example = "세종대학교 학식당")
    String transactionContent,

    @ApiModelProperty(value = "지출 유형", example = "지출")
    TransactionType transactionType,

    @ApiModelProperty(value = "카테고리", example = "식비")
    ExpenseCategory expenseCategory,

    @ApiModelProperty(value = "거래 금액", example = "10000")
    Integer transactionAmount,

    @ApiModelProperty(value = "거래 일시", example = "2026-07-25T00:00:00")
    String transactionAt,

    @ApiModelProperty(value = "거래 메모", example = "학식당에서 스팸순두부찌개")
    String transactionMemo
) {

    public static TransactionResponseDTO from(TransactionVO vo) {
        return TransactionResponseDTO.builder()
            .transactionContent(vo.getTransactionContent())
            .transactionType(vo.getTransactionType())
            .expenseCategory(vo.getExpenseCategory())
            .transactionAmount(vo.getTransactionAmount())
            .transactionAt(vo.getTransactionAt() != null ? vo.getTransactionAt().toString() : null)
            .transactionMemo(vo.getTransactionMemo())
            .build();
    }

    public static List<TransactionResponseDTO> fromList(List<TransactionVO> voList){
        if(voList == null || voList.isEmpty()){
            return List.of();
        }
        return voList.stream().map(TransactionResponseDTO::from).toList();
    }
}