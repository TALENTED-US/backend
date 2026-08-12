package com.talented.buttie.catalog.controller;

import com.talented.buttie.catalog.domain.PolicyVO;
import com.talented.buttie.catalog.dto.response.PolicyResponse;
import com.talented.buttie.catalog.service.PolicyService;
import com.talented.buttie.common.response.ApplicationResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Policy")
@RestController
@RequestMapping("/api/catalog/policy")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @ApiOperation("정책 목록 전체 조회")
    @GetMapping("")
    public ApplicationResponse<List<PolicyResponse>> getAllPolicies() {
        List<PolicyVO> policies = policyService.getAllPolicies();

        List<PolicyResponse> responseList = policies.stream()
            .map(PolicyResponse::from)
            .toList();

        return ApplicationResponse.onSuccess(responseList);
    }
}
