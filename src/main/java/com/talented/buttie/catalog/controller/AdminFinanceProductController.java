package com.talented.buttie.catalog.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.talented.buttie.common.response.ApplicationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Admin Finance Product")
@RestController
@RequestMapping("/admin/financial-products")
@RequiredArgsConstructor
public class AdminFinanceProductController {

    @ApiOperation("금융 상품 목록 검색 및 조회")
    @GetMapping
    public ApplicationResponse<Void> getFinancialProducts() {
        return null;
    }

    @ApiOperation("금융 상품 수동 등록")
    @PostMapping
    public ApplicationResponse<Void> createFinancialProduct() {
        return null;
    }

    @ApiOperation("금융 상품 가입 조건 수정")
    @PutMapping("/{productId}")
    public ApplicationResponse<Void> updateFinancialProduct(@PathVariable Long productId) {
        return null;
    }

    @ApiOperation("금융 상품 노출 제어")
    @PatchMapping("/{productId}/status")
    public ApplicationResponse<Void> updateFinancialProductStatus(@PathVariable Long productId) {
        return null;
    }

    @ApiOperation("금융 상품 정보 영구 삭제")
    @DeleteMapping("/{productId}")
    public ApplicationResponse<Void> deleteFinancialProduct(@PathVariable Long productId) {
        return null;
    }
}

