package com.talented.buttie.ledger.dto.response.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.ledger.domain.TransactionType;
import com.talented.buttie.ledger.domain.TransactionSource;
import com.talented.buttie.ledger.domain.ClassificationMethod;
import com.talented.buttie.ledger.domain.TransactionVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;
import lombok.Builder;

import com.talented.buttie.common.util.PKCrypto;

@ApiModel(description = "거래 목록 조회 응답")
@Builder
public record TransactionResponse(
    @ApiModelProperty(value = "암호화된 사용자 ID", example = "ToJn4pdJ9axT1Oay6sf2wQ")
    String userId,

    @ApiModelProperty(value = "암호화된 거래 ID", example = "xX79VwugC283X2XVQTkp1Q")
    String transactionId,

    @ApiModelProperty(value = "거래 내용", example = "String")
    @NotBlank(message = "거래 내용은 필수입니다.")
    String transactionContent,

    @ApiModelProperty(value = "거래 유형", example = "EXPENSE, INCOME, FIXED, TRANSFER")
    TransactionType transactionType,

    @ApiModelProperty(value = "거래 출처", example = "ACCOUNT, CARD, MANUAL")
    TransactionSource transactionSource,

    @ApiModelProperty(value = "분류 방식", example = "MERCHANT_REGNO, ACCOUNT_INFLOW, USER_CONFIRMED, UNCLASSIFIED, MANUAL")
    ClassificationMethod classificationMethod,

    @ApiModelProperty(value = "가맹점명")
    String merchantName,

    @ApiModelProperty(value = "가맹점 사업자등록번호")
    String merchantRegistrationNumber,

    @ApiModelProperty(value = "카테고리", example = "FOOD, TRANSPORT, HOUSING, COMMUNICATION, SUBSCRIPTION, EDUCATION, CERTIFICATE, ETC_EXPENSE")
    ExpenseCategory expenseCategory,

    @ApiModelProperty(value = "거래 금액", example = "Integer")
    Integer transactionAmount,

    @ApiModelProperty(value = "거래 일시", example = "yyyy-MM-ddTHH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime transactionAt,

    @ApiModelProperty(value = "거래 메모", example = "String")
    String transactionMemo,

    @ApiModelProperty(value = "분석 제외 여부", example = "false")
    Boolean analysisExcluded
) {

    public static TransactionResponse from(TransactionVO vo) {
        return TransactionResponse.builder()
            .userId(PKCrypto.encrypt(vo.getUserId()))
            .transactionId(PKCrypto.encrypt(vo.getTransactionId()))
            .transactionContent(vo.getTransactionContent())
            .transactionType(vo.getTransactionType())
            .transactionSource(vo.getTransactionSource())
            .classificationMethod(vo.getClassificationMethod())
            .merchantName(vo.getMerchantName())
            .merchantRegistrationNumber(vo.getMerchantRegistrationNumber())
            .expenseCategory(vo.getExpenseCategory())
            .transactionAmount(vo.getTransactionAmount())
            .transactionAt(vo.getTransactionAt())
            .transactionMemo(vo.getTransactionMemo())
            .analysisExcluded(vo.getAnalysisExcluded())
            .build();
    }
}
