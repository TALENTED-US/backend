package com.talented.buttie.ledger.dto.response;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.ledger.domain.TransactionType;
import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.ledger.exception.LedgerErrorCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@ApiModel(description = "거래 목록 조회 응답")
@Builder
public record TransactionResponseDTO(
    @ApiModelProperty(value = "거래 내용", example = "세종대학교 학식당")
    String content,

    @ApiModelProperty(value = "지출 유형", example = "지출")
    TransactionType transactionType,

    @ApiModelProperty(value = "카테고리", example = "식비")
    ExpenseCategory category,

    @ApiModelProperty(value = "거래 금액", example = "10000")
    Integer amount,

    @ApiModelProperty(value = "거래 일시", example = "2026-07-25T:00:00:00")
    LocalDateTime transactionAt,

    @ApiModelProperty(value = "거래 메모", example = "학식당에서 스팸순두부찌개")
    String memo
) {

    public static TransactionResponseDTO from(TransactionVO vo) {
        return TransactionResponseDTO.builder()
            .content(vo.getContent())
            .transactionType(vo.getTransactionType())
            .category(vo.getCategory())
            .amount(vo.getAmount())
            .transactionAt(vo.getTransactionAt())
            .memo(vo.getMemo())
            .build();
    }

    public static List<TransactionResponseDTO> fromList(List<TransactionVO> voList){
        if(voList == null || voList.isEmpty()){
            return List.of();
        }
        return voList.stream().map(TransactionResponseDTO::from).toList();
    }
}