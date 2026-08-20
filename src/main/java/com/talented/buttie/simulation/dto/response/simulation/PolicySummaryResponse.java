package com.talented.buttie.simulation.dto.response.simulation;

import com.talented.buttie.catalog.domain.PolicyStatus;
import com.talented.buttie.catalog.domain.PolicyCategory;
import com.talented.buttie.catalog.domain.PolicyVO;
import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.simulation.domain.SimulationItemVO;
import com.talented.buttie.simulation.domain.SimulationVO;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import java.time.YearMonth;
import lombok.Builder;

@Builder
public record PolicySummaryResponse(

    @ApiModelProperty(value = "암호화된 정책 ID")
    String policyId,

    @ApiModelProperty(value = "정책명", example = "청년월세 특별지원")
    String policyName,

    @ApiModelProperty(value = "정책 카테고리", example = "주거")
    PolicyCategory policyCategory,

    @ApiModelProperty(value = "지원 대상 최소 나이", example = "19")
    Integer policyMinAge,

    @ApiModelProperty(value = "지원 대상 최대 나이", example = "34")
    Integer policyMaxAge,

    @ApiModelProperty(value = "지원 대상 지역", example = "전국")
    String policyRegion,

    @ApiModelProperty(value = "회차당 정책 지원 금액", example = "200000")
    Integer policySupportAmount,

    @ApiModelProperty(value = "정책의 전체 지원 개월 수", example = "12")
    Integer supportMonthCount,

    @ApiModelProperty(value = "시뮬레이션 기간에 실제 적용된 지원 개월 수", example = "5")
    Integer appliedSupportMonthCount,

    @ApiModelProperty(value = "시뮬레이션 기간 때문에 일부 지원이 제외되었는지 여부", example = "true")
    Boolean supportTruncated,

    @ApiModelProperty(value = "지원 기간 제외 안내 문구", example = "시뮬레이션 기간 밖이라 7개월 지원이 제외되었습니다.")
    String supportTruncatedMessage,

    @ApiModelProperty(value = "정책 신청 마감 일시", example = "2026-12-31T23:59:59")
    LocalDate dueDate,

    @ApiModelProperty(value = "필요 서류", example = "주민등록등본, 임대차계약서, 통장사본")
    String requiredDocument,

    @ApiModelProperty(value = "요구되는 취업 준비 상태", example = "미취업")
    String employmentPrepStatus,

    @ApiModelProperty(value = "지원 대상 가구원 수", example = "1")
    Integer familyCount,

    @ApiModelProperty(value = "정책 상태", example = "AVAILABLE")
    PolicyStatus policyStatus,

    @ApiModelProperty(value = "정책 상세 URL", example = "https://www.gov.kr/youth-housing")
    String policyUrl
) {

    public static PolicySummaryResponse from(
        SimulationItemVO item,
        PolicyVO policy,
        SimulationVO simulation
    ) {
        if (policy == null) {
            return null;
        }

        int totalSupportMonthCount = policy.getSupportMonthCount() == null
            ? 1
            : Math.max(policy.getSupportMonthCount(), 1);
        int appliedSupportMonthCount = calculateAppliedSupportMonthCount(item, simulation, totalSupportMonthCount);
        int truncatedSupportMonthCount = Math.max(totalSupportMonthCount - appliedSupportMonthCount, 0);
        boolean supportTruncated = truncatedSupportMonthCount > 0;

        return PolicySummaryResponse.builder()
            .policyId(PKCrypto.encrypt(policy.getPolicyId()))
            .policyName(policy.getPolicyName())
            .policyCategory(policy.getPolicyCategory())
            .policyMinAge(policy.getPolicyMinAge())
            .policyMaxAge(policy.getPolicyMaxAge())
            .policyRegion(policy.getPolicyRegion())
            .policySupportAmount(policy.getPolicySupportAmount())
            .supportMonthCount(totalSupportMonthCount)
            .appliedSupportMonthCount(appliedSupportMonthCount)
            .supportTruncated(supportTruncated)
            .supportTruncatedMessage(resolveSupportTruncatedMessage(truncatedSupportMonthCount))
            .dueDate(policy.getDueDate())
            .requiredDocument(policy.getRequiredDocument())
            .employmentPrepStatus(policy.getEmploymentPrepStatus())
            .familyCount(policy.getFamilyCount())
            .policyStatus(policy.getPolicyStatus())
            .policyUrl(policy.getPolicyUrl())
            .build();
    }

    private static int calculateAppliedSupportMonthCount(
        SimulationItemVO item,
        SimulationVO simulation,
        int supportMonthCount
    ) {
        if (item.getApplyStartDate() == null
            || simulation.getSimulationStartDate() == null
            || simulation.getSimulationDueDate() == null) {
            return 0;
        }

        YearMonth startMonth = YearMonth.from(item.getApplyStartDate());
        YearMonth endMonth = startMonth.plusMonths(supportMonthCount - 1L);
        int monthCount = 0;

        for (YearMonth month = startMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
            int recurrenceDay = Math.min(item.getApplyStartDate().getDayOfMonth(), month.lengthOfMonth());
            LocalDate supportDate = month.atDay(recurrenceDay);
            if (!supportDate.isBefore(simulation.getSimulationStartDate())
                && !supportDate.isAfter(simulation.getSimulationDueDate())) {
                monthCount++;
            }
        }

        return monthCount;
    }

    private static String resolveSupportTruncatedMessage(int truncatedSupportMonthCount) {
        if (truncatedSupportMonthCount <= 0) {
            return null;
        }

        return "시뮬레이션 기간 밖이라 " + truncatedSupportMonthCount + "개월 지원이 제외되었습니다.";
    }
}
