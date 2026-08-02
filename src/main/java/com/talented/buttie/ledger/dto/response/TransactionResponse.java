package com.talented.buttie.ledger.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.ledger.domain.TransactionType;
import com.talented.buttie.ledger.domain.TransactionVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.NotBlank;
import lombok.Builder;

import com.talented.buttie.common.util.PKCrypto;

@ApiModel(description = "거래 목록 조회 응답")
@Builder
public record TransactionResponse(
    @ApiModelProperty(value = "암호화된 사용자 ID", example = "exp123...")
    String userId,

    @ApiModelProperty(value = "거래 내용", example = "String")
    @NotBlank(message = "거래 내용은 필수입니다.")
    String transactionContent,

    @ApiModelProperty(value = "지출 유형", example = "EXPENSE, INCOME, FIXED")
    TransactionType transactionType,

    @ApiModelProperty(value = "카테고리", example = "FOOD, TRANSPORT, HOUSING, COMMUNICATION, SUBSCRIPTION, EDUCATION, CERTIFICATE, ETC_EXPENSE")
    ExpenseCategory expenseCategory,

    @ApiModelProperty(value = "거래 금액", example = "Integer")
    Integer transactionAmount,

    @ApiModelProperty(value = "거래 일시", example = "yyyy-MM-ddTHH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime transactionAt,

    @ApiModelProperty(value = "거래 메모", example = "String")
    String transactionMemo
) {

    public static TransactionResponse from(TransactionVO vo) {
        return TransactionResponse.builder()
            .userId(PKCrypto.encrypt(vo.getUserId()))
            .transactionContent(vo.getTransactionContent())
            .transactionType(vo.getTransactionType())
            .expenseCategory(vo.getExpenseCategory())
            .transactionAmount(vo.getTransactionAmount())
            .transactionAt(vo.getTransactionAt())
            .transactionMemo(vo.getTransactionMemo())
            .build();
    }
}