package com.talented.buttie.ledger.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.ledger.domain.TransactionVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.Builder;

@ApiModel(description = "거래 상세 조회 응답")
@Builder
public record TransactionDetailResponse(
    @ApiModelProperty(value = "암호화된 사용자 ID", example = "exp123...")
    String userId,

    @ApiModelProperty(value = "거래 내용", example = "String", required = true)
    @NotBlank(message = "거래 내용은 필수입니다.")
    String transactionContent,

    @ApiModelProperty(value = "거래 일시", example = "2026-08-03T12:30:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime transactionAt,

    @ApiModelProperty(value = "거래 메모", example = "String")
    String transactionMemo,

    @ApiModelProperty(value = "거래 금액", example = "Integer", required = true)
    @NotNull
    @Positive(message = "거래 금액은 0보다 커야합니다.")
    Integer transactionAmount
) {

    public static TransactionDetailResponse from(TransactionVO vo) {
        return TransactionDetailResponse.builder()
            .userId(PKCrypto.encrypt(vo.getUserId()))
            .transactionContent(vo.getTransactionContent())
            .transactionAt(vo.getTransactionAt())
            .transactionMemo(vo.getTransactionMemo())
            .transactionAmount(vo.getTransactionAmount())
            .build();
    }
}
