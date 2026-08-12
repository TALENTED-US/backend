package com.talented.buttie.catalog.controller;

import com.talented.buttie.catalog.dto.request.PolicySearchRequest;
import com.talented.buttie.catalog.dto.response.PolicyResponse;
import com.talented.buttie.catalog.service.PolicyService;
import com.talented.buttie.common.dto.PageResponse;
import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.common.security.AuthenticationUser;
import com.talented.buttie.common.security.annotation.AuthUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Policy")
@RestController
@RequestMapping("/api/catalog/policy")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @ApiOperation("정책 목록 조회 및 GET Query Param 조건 필터링 (페이지네이션 10개씩 지원)")
    @GetMapping("")
    public ApplicationResponse<PageResponse<PolicyResponse>> getAllPolicies(
        @AuthUser AuthenticationUser user,
        @ApiParam(value = "검색 키워드 (정책명, 필요서류)") @RequestParam(value = "keyword", required = false) String keyword,
        @ApiParam(value = "정책 카테고리 (주거, 교통, 복지, 취업, 교육, 청년지원)") @RequestParam(value = "category", required = false) String category,
        @ApiParam(value = "정책 지역 (서울, 경기, 인천, 부산, 대구, 광주, 대전, 울산, 세종, 전국 또는 우편번호 zipCd)") @RequestParam(value = "policyRegion", required = false) String policyRegion,
        @ApiParam(value = "사용자 나이") @RequestParam(value = "age", required = false) Integer age,
        @ApiParam(value = "취업 준비 상태 (첫취업, 재취업, 재직자, 예비창업자, 미취업자)") @RequestParam(value = "employmentPrepStatus", required = false) String employmentPrepStatus,
        @ApiParam(value = "최소 지원 금액") @RequestParam(value = "minAmount", required = false) Integer minAmount,
        @ApiParam(value = "신청 마감 필터 (오늘 마감, 3일 이내, 7일 이내, 30일 이내, 상시)") @RequestParam(value = "dueDateFilter", required = false) String dueDateFilter,
        @ApiParam(value = "페이지 번호 (1부터 시작, 기본값: 1)", defaultValue = "1") @RequestParam(value = "page", defaultValue = "1") int page,
        @ApiParam(value = "페이지 당 항목 수 (기본값: 10)", defaultValue = "10") @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Long userId = user != null ? user.userId() : null;

        PolicySearchRequest request = PolicySearchRequest.builder()
            .keyword(keyword)
            .category(category)
            .policyRegion(policyRegion)
            .age(age)
            .employmentPrepStatus(employmentPrepStatus)
            .minAmount(minAmount)
            .dueDateFilter(dueDateFilter)
            .page(page)
            .size(size)
            .build();

        PageResponse<PolicyResponse> pageResponse = policyService.searchPolicies(userId, request);
        return ApplicationResponse.onSuccess(pageResponse);
    }

    @ApiOperation("정책 목록 검색 및 POST Body 조건별 상세 필터링 (페이지네이션 10개씩 지원)")
    @PostMapping("/search")
    public ApplicationResponse<PageResponse<PolicyResponse>> searchPolicies(
        @AuthUser AuthenticationUser user,
        @RequestBody(required = false) PolicySearchRequest request
    ) {
        Long userId = user != null ? user.userId() : null;
        PageResponse<PolicyResponse> pageResponse = policyService.searchPolicies(userId, request);

        return ApplicationResponse.onSuccess(pageResponse);
    }
}
