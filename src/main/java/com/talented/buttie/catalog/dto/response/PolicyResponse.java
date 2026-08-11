package com.talented.buttie.catalog.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.talented.buttie.catalog.domain.PolicyVO;
import com.talented.buttie.common.util.PKCrypto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import lombok.Builder;

@ApiModel(description = "정책 목록 조회 응답")
@Builder
public record PolicyResponse(
    @ApiModelProperty(value = "암호화된 정책 ID", example = "xX79VwugC283X2XVQTkp1Q")
    String policyId,

    @ApiModelProperty(value = "정부지원 정책 이름", example = "청년월세 특별지원")
    String policyName,

    @ApiModelProperty(value = "정책 지원 금액", example = "200000")
    Integer policySupportAmount,

    @ApiModelProperty(value = "지원 월수", example = "12")
    Integer supportMonthCount,

    @ApiModelProperty(value = "신청 마감일", example = "2026-12-31")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate dueDate,

    @ApiModelProperty(value = "제출 필요 서류", example = "임대차계약서, 월세납입증명서")
    String requiredDocument,

    @ApiModelProperty(value = "정책 신청 URL", example = "https://www.gov.kr/youth-housing")
    String policyUrl
) {

    public static PolicyResponse from(PolicyVO vo) {
        if (vo == null) return null;

        return PolicyResponse.builder()
            .policyId(vo.getPolicyId() == null ? null : PKCrypto.encrypt(vo.getPolicyId()))
            .policyName(vo.getPolicyName())
            .policySupportAmount(vo.getPolicySupportAmount())
            .supportMonthCount(vo.getSupportMonthCount())
            .dueDate(vo.getDueDate())
            .requiredDocument(vo.getRequiredDocument())
            .policyUrl(vo.getPolicyUrl())
            .build();
    }
}
