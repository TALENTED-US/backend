package com.talented.buttie.dashboard.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.common.security.AuthenticationUser;
import com.talented.buttie.common.security.annotation.AuthUser;
import com.talented.buttie.dashboard.dto.TimelineResponse;
import com.talented.buttie.dashboard.service.TimelineReadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Timeline")
@RestController
@RequestMapping("/api/timeline")
@RequiredArgsConstructor
public class TimelineController {
    private final TimelineReadService timelineReadService;

    @ApiOperation("월별 재정 타임라인 조회")
    @GetMapping
    public ApplicationResponse<TimelineResponse> getTimeline(
        @AuthUser AuthenticationUser user
    ){
        Long targetUserId = user.userId();
        TimelineResponse response = timelineReadService.getTimeline(targetUserId);
        return ApplicationResponse.onSuccess(response);
    }
}
