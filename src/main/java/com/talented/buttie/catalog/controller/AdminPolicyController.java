package com.talented.buttie.catalog.controller;

import com.talented.buttie.catalog.domain.AmountParseConfidence;
import com.talented.buttie.catalog.dto.request.AdminPolicyUpsertRequest;
import com.talented.buttie.catalog.dto.response.AdminPolicyListResponse;
import com.talented.buttie.catalog.dto.response.AdminPolicyResponse;
import com.talented.buttie.catalog.service.AdminPolicyService;
import com.talented.buttie.catalog.service.PolicyIngestionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.talented.buttie.common.response.ApplicationResponse;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Admin Policy")
@RestController
@RequestMapping("/admin/policies")
@RequiredArgsConstructor
public class AdminPolicyController {

    private final AdminPolicyService adminPolicyService;
    private final PolicyIngestionService policyIngestionService;

    @ApiOperation("온통청년 정책 수동 수집 실행")
    @PostMapping("/ingest")
    public ApplicationResponse<Void> ingestPolicies() {
        policyIngestionService.ingestYouthCenterPolicies();
        return ApplicationResponse.onSuccess();
    }

    @ApiOperation("정부 지원 정책 목록 통합 관리")
    @GetMapping
    public ApplicationResponse<AdminPolicyListResponse> getPolicies(
        @RequestParam(required = false) String externalSource,
        @RequestParam(required = false) AmountParseConfidence amountParseConfidence,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ApplicationResponse.onSuccess(
            adminPolicyService.getPolicies(externalSource, amountParseConfidence, page, size)
        );
    }

    @ApiOperation("정부 지원 정책 등록")
    @PostMapping
    public ApplicationResponse<AdminPolicyResponse> createPolicy(@Valid @RequestBody AdminPolicyUpsertRequest request) {
        return ApplicationResponse.onSuccess(adminPolicyService.createPolicy(request));
    }

    @ApiOperation("등록 정부 지원 정책 수정")
    @PutMapping("/{policyId}")
    public ApplicationResponse<AdminPolicyResponse> updatePolicy(
        @PathVariable Long policyId,
        @Valid @RequestBody AdminPolicyUpsertRequest request
    ) {
        return ApplicationResponse.onSuccess(adminPolicyService.updatePolicy(policyId, request));
    }

    @ApiOperation("등록 정부 지원 정책 삭제")
    @DeleteMapping("/{policyId}")
    public ApplicationResponse<Void> deletePolicy(@PathVariable Long policyId) {
        adminPolicyService.deletePolicy(policyId);
        return ApplicationResponse.onSuccess();
    }
}
