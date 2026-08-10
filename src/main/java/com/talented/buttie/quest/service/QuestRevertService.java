package com.talented.buttie.quest.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.quest.domain.QuestStatus;
import com.talented.buttie.quest.domain.QuestVO;
import com.talented.buttie.quest.exception.QuestErrorCode;
import com.talented.buttie.quest.mapper.QuestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuestRevertService {

    private final QuestMapper questMapper;
    private final ExperienceService experienceService;

    @Transactional
    public Long revertQuest(Long userId, Long questId) {
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

        // 완료 상태인 퀘스트만 취소(진행중 변경) 가능
        if (quest.getQuestStatus() != QuestStatus.COMPLETED) {
            throw ApplicationException.from(QuestErrorCode.QUEST_NOT_COMPLETED);
        }

        try {
            // 1. 퀘스트 상태를 NOT_COMPLETED (진행중)으로 변경
            questMapper.updateStatus(questId, QuestStatus.NOT_COMPLETED);
        } catch (Exception e) {
            throw ApplicationException.from(QuestErrorCode.QUEST_STATUS_UPDATE_FAILED);
        }

        try {
            // 2. 퀘스트 완료 시 지급받은 경험치 회수 (차감)
            Integer expReward = quest.getExpReward();
            if (expReward != null && expReward > 0) {
                experienceService.deductExperience(userId, expReward);
            }
        } catch (Exception e) {
            throw ApplicationException.from(QuestErrorCode.QUEST_EXP_REVERT_FAILED);
        }

        return questId;
    }
}
