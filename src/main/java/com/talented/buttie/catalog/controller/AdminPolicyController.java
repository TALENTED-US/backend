package com.talented.buttie.catalog.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.talented.buttie.common.response.ApplicationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Admin Policy")
@RestController
@RequestMapping("/admin/policies")
@RequiredArgsConstructor
public class AdminPolicyController {

    @ApiOperation("정부 지원 정책 목록 통합 관리")
    @GetMapping
    public ApplicationResponse<Void> getPolicies() {
        return null;
    }

    @ApiOperation("정부 지원 정책 등록")
    @PostMapping
    public ApplicationResponse<Void> createPolicy() {
        return null;
    }

    @ApiOperation("등록 정부 지원 정책 수정")
    @PutMapping("/{policyId}")
    public ApplicationResponse<Void> updatePolicy(@PathVariable Long policyId) {
        return null;
    }

    @ApiOperation("등록 정부 지원 정책 삭제")
    @DeleteMapping("/{policyId}")
    public ApplicationResponse<Void> deletePolicy(@PathVariable Long policyId) {
        return null;
    }
}

