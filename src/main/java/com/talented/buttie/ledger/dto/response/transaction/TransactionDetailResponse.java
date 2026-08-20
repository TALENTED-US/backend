package com.talented.buttie.ledger.dto.response.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.ledger.domain.ClassificationMethod;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.ledger.domain.TransactionSource;
import com.talented.buttie.ledger.domain.TransactionType;
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
    @ApiModelProperty(value = "암호화된 사용자 ID", example = "ToJn4pdJ9axT1Oay6sf2wQ")
    String userId,

    @ApiModelProperty(value = "암호화된 거래 ID", example = "xX79VwugC283X2XVQTkp1Q")
    String transactionId,

    @ApiModelProperty(value = "거래 내용", example = "String", required = true)
    @NotBlank(message = "거래 내용은 필수입니다.")
    String transactionContent,

    @ApiModelProperty(value = "거래 유형", example = "EXPENSE, INCOME, FIXED, TRANSFER")
    TransactionType transactionType,

    @ApiModelProperty(value = "거래 출처", example = "ACCOUNT, CARD, MANUAL")
    TransactionSource transactionSource,

    @ApiModelProperty(value = "분류 방식", example = "MERCHANT_REGNO, MERCHANT_NAME, ACCOUNT_INFLOW, USER_CONFIRMED, UNCLASSIFIED, MANUAL")
    ClassificationMethod classificationMethod,

    @ApiModelProperty(value = "가맹점명")
    String merchantName,

    @ApiModelProperty(value = "가맹점 사업자등록번호")
    String merchantRegistrationNumber,

    @ApiModelProperty(value = "지출 카테고리", example = "HOUSING_COMMUNICATION")
    ExpenseCategory expenseCategory,

    @ApiModelProperty(value = "거래 일시", example = "2026-08-03T12:30:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime transactionAt,

    @ApiModelProperty(value = "거래 메모", example = "String")
    String transactionMemo,

    @ApiModelProperty(value = "거래 금액", example = "Integer", required = true)
    @NotNull
    @Positive(message = "거래 금액은 0보다 커야합니다.")
    Integer transactionAmount,

    @ApiModelProperty(value = "분석 제외 여부", example = "true")
    Boolean analysisExcluded
) {

    public static TransactionDetailResponse from(TransactionVO vo) {
        return TransactionDetailResponse.builder()
            .userId(PKCrypto.encrypt(vo.getUserId()))
            .transactionId(PKCrypto.encrypt(vo.getTransactionId()))
            .transactionContent(vo.getTransactionContent())
            .transactionType(vo.getTransactionType())
            .transactionSource(vo.getTransactionSource())
            .classificationMethod(vo.getClassificationMethod())
            .merchantName(vo.getMerchantName())
            .merchantRegistrationNumber(vo.getMerchantRegistrationNumber())
            .expenseCategory(vo.getExpenseCategory())
            .transactionAt(vo.getTransactionAt())
            .transactionMemo(vo.getTransactionMemo())
            .transactionAmount(vo.getTransactionAmount())
            .analysisExcluded(vo.getAnalysisExcluded())
            .build();
    }
}
