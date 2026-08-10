package com.talented.buttie.quest.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.common.security.AuthenticationUser;
import com.talented.buttie.common.security.annotation.AuthUser;
import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.quest.dto.response.QuestResponse;
import com.talented.buttie.quest.service.QuestReadService;
import com.talented.buttie.quest.service.QuestUpdateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Quest")
@RestController
@RequestMapping("/api/quest")
@RequiredArgsConstructor
public class QuestController {

    private final QuestReadService questReadService;
    private final QuestUpdateService questUpdateService;

    @ApiOperation("행동과제(퀘스트) 목록 조회")
    @GetMapping("")
    public ApplicationResponse<List<QuestResponse>> getQuests(
        @AuthUser AuthenticationUser user
    ) {
        Long targetUserId = user.userId();
        List<QuestResponse> response = questReadService.getQuests(targetUserId);
        return ApplicationResponse.onSuccess(response);
    }

    @ApiOperation("퀘스트 완료 처리")
    @PatchMapping("/{questId}/complete")
    public ApplicationResponse<String> completeQuest(
        @PathVariable("questId") String questId,
        @AuthUser AuthenticationUser user
    ) {
        Long targetUserId = user.userId();
        Long decryptedQuestId = PKCrypto.decrypt(questId);
        Long resultQuestId = questUpdateService.completeQuest(targetUserId, decryptedQuestId);
        return ApplicationResponse.onSuccess(PKCrypto.encrypt(resultQuestId));
    }
}
