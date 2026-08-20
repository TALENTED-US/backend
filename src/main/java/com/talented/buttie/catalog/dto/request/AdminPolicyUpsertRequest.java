package com.talented.buttie.catalog.dto.request;

import com.talented.buttie.catalog.domain.PolicyCategory;
import com.talented.buttie.catalog.domain.PolicyStatus;
import java.time.LocalDate;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 관리자 수동 등록/수정 공통 요청. 저장 시 externalSource=null로 처리해 수집 정책과 구분한다.
 */
@Builder
public record AdminPolicyUpsertRequest(
    @NotBlank(message = "정책명은 필수입니다.")
    String policyName,

    @NotNull(message = "정책 카테고리는 필수입니다.")
    PolicyCategory policyCategory,

    Integer policyMinAge,
    Integer policyMaxAge,
    String policyRegion,
    Integer policySupportAmount,
    Integer supportMonthCount,
    LocalDate dueDate,
    String requiredDocument,
    String employmentPrepStatus,
    Integer familyCount,

    @NotNull(message = "정책 상태는 필수입니다.")
    PolicyStatus policyStatus,

    String policyUrl
) {
}
