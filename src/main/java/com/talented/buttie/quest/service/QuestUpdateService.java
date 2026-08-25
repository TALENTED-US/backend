package com.talented.buttie.quest.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.quest.domain.QuestVO;
import com.talented.buttie.quest.exception.QuestErrorCode;
import com.talented.buttie.quest.mapper.QuestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuestUpdateService {

    private final QuestMapper questMapper;
    private final ExperienceService experienceService;

    @Transactional
    public Long completeQuest(Long userId, Long questId) {
        if (questId == null) {
            throw ApplicationException.from(QuestErrorCode.QUEST_NOT_FOUND);
        }

        QuestVO quest = questMapper.findById(questId);

        if (quest == null) {
            throw ApplicationException.from(QuestErrorCode.QUEST_NOT_FOUND);
        }

        if (!quest.getUserId().equals(userId)) {
            throw ApplicationException.from(QuestErrorCode.QUEST_USER_ID_MISMATCH);
        }

        try {
            // 1. NOT_COMPLETED 상태일 때만 원자적으로 COMPLETED로 갱신 (동시 요청 중복 완료/경험치 적립 방지)
            int updatedRows = questMapper.completeQuestStatus(questId);
            if (updatedRows == 0) {
                throw ApplicationException.from(QuestErrorCode.QUEST_ALREADY_COMPLETED);
            }
        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw ApplicationException.from(QuestErrorCode.QUEST_STATUS_UPDATE_FAILED);
        }

        try {
            // 2. 상태 전환이 정상 성공(영향 행 수 = 1)한 경우에만 경험치 지급
            Integer expReward = quest.getExpReward();
            if (expReward != null && expReward > 0) {
                experienceService.addExperience(userId, expReward);
            }
        } catch (Exception e) {
            throw ApplicationException.from(QuestErrorCode.QUEST_EXP_GRANT_FAILED);
        }

        return questId;
    }
}
