package com.talented.buttie.ledger.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.common.security.AuthenticationUser;
import com.talented.buttie.common.security.annotation.AuthUser;
import com.talented.buttie.ledger.domain.calendar.CalendarMonthlyVO;
import com.talented.buttie.ledger.dto.response.calendar.GetCalendarMonthlyResponse;
import com.talented.buttie.ledger.service.calendar.ReadCalendarMonthlyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Calendar")
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final ReadCalendarMonthlyService readCalendarMonthlyService;

    @ApiOperation("가계부 캘린더 월간 데이터 조회")
    @GetMapping("")
    public ApplicationResponse<GetCalendarMonthlyResponse> getCalendarMonthly(
        @RequestParam("year") int year,
        @RequestParam("month") int month,
        @AuthUser AuthenticationUser user
    ) {
        Long targetUserId = user.userId();

        CalendarMonthlyVO vo = readCalendarMonthlyService.getCalendarMonthly(targetUserId, year, month);

        return ApplicationResponse.onSuccess(GetCalendarMonthlyResponse.from(vo));
    }
}