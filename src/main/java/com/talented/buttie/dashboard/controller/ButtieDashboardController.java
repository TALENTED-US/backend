package com.talented.buttie.dashboard.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.common.security.AuthenticationUser;
import com.talented.buttie.common.security.annotation.AuthUser;
import com.talented.buttie.dashboard.dto.ButtieDashboardResponse;
import com.talented.buttie.dashboard.service.ButtieDashboardReadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Dashboard")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class ButtieDashboardController {

    private final ButtieDashboardReadService buttieDashboardReadService;

    @ApiOperation("홈 화면 버티 대시보드 조회")
    @GetMapping("/buttie")
    public ApplicationResponse<ButtieDashboardResponse> getButtieDashboard(
        @AuthUser AuthenticationUser user
    ) {
        Long targetUserId = user.userId();
        ButtieDashboardResponse response = buttieDashboardReadService.getButtieDashboard(targetUserId);
        return ApplicationResponse.onSuccess(response);
    }
}
