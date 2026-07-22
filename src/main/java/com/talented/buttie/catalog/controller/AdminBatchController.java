package com.talented.buttie.catalog.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.talented.buttie.common.response.ApplicationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Admin Batch")
@RestController
@RequestMapping("/admin/batches")
@RequiredArgsConstructor
public class AdminBatchController {

    @ApiOperation("배치 실행 이력 조회")
    @GetMapping
    public ApplicationResponse<Void> getBatchHistories() {
        return null;
    }

    @ApiOperation("배치 재실행")
    @PostMapping("/{batchId}/retry")
    public ApplicationResponse<Void> retryBatch(@PathVariable Long batchId) {
        return null;
    }
}
