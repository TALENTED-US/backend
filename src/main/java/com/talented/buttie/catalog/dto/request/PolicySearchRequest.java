package com.talented.buttie.catalog.dto.request;

import com.talented.buttie.catalog.domain.PolicyStatus;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record PolicySearchRequest(
    @ApiModelProperty(value = "검색 키워드 (정책명, 필요서류)", example = "월세")
    String keyword,

    @ApiModelProperty(value = "정책 카테고리 (주거, 교통, 복지, 취업, 교육, 청년지원 또는 HOUSING, TRANSPORT, WELFARE, EMPLOYMENT, EDUCATION, YOUTH_SUPPORT)", example = "주거")
    String policyCategory,

    @ApiModelProperty(value = "정책 지역 (서울, 경기, 인천, 부산, 대구, 광주, 대전, 울산, 세종, 전국 또는 우편번호 zipCd)", example = "부산")
    String policyRegion,

    @ApiModelProperty(value = "사용자 나이", example = "25")
    Integer age,

    @ApiModelProperty(value = "지원 금액 필터", example = "200000")
    Integer policySupportAmount,

    @ApiModelProperty(value = "신청 마감일 (YYYY-MM-DD)", example = "2026-12-31")
    LocalDate dueDate,

    @ApiModelProperty(value = "신청 마감 필터 (오늘 마감, 3일 이내, 7일 이내, 30일 이내, 상시 또는 TODAY, WITHIN_3_DAYS, WITHIN_7_DAYS, WITHIN_30_DAYS, ALWAYS)", example = "상시")
    String dueDateFilter,

    @ApiModelProperty(value = "취업 준비 상태 (첫취업, 재취업, 재직자, 예비창업자, 미취업자 또는 FIRST_JOB, REEMPLOYMENT, EMPLOYED, PROSPECTIVE_FOUNDER, UNEMPLOYED)", example = "미취업자")
    String employmentPrepStatus,

    @ApiModelProperty(value = "정책 상태 (기본값: AVAILABLE)", example = "AVAILABLE")
    PolicyStatus policyStatus,

    @ApiModelProperty(value = "페이지 번호 (1부터 시작, 기본값: 1)", example = "1")
    Integer page,

    @ApiModelProperty(value = "페이지 당 항목 수 (기본값: 10)", example = "10")
    Integer size
) {
    public int getPageNumber() {
        return (page == null || page < 1) ? 1 : page;
    }

    public int getPageSize() {
        return (size == null || size < 1) ? 10 : size;
    }

    public int getOffset() {
        return (getPageNumber() - 1) * getPageSize();
    }
}
