package com.talented.buttie.quest.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.common.security.AuthenticationUser;
import com.talented.buttie.common.security.annotation.AuthUser;
import com.talented.buttie.quest.dto.response.QuestResponse;
import com.talented.buttie.quest.service.QuestReadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Quest")
@RestController
@RequestMapping("/api/quest")
@RequiredArgsConstructor
public class QuestController {

    private final QuestReadService questReadService;

    @ApiOperation("행동과제(퀘스트) 목록 조회")
    @GetMapping("")
    public ApplicationResponse<List<QuestResponse>> getQuests(
        @AuthUser AuthenticationUser user
    ) {
        Long targetUserId = user.userId();
        List<QuestResponse> response = questReadService.getQuests(targetUserId);
        return ApplicationResponse.onSuccess(response);
    }
}
