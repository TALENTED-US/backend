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
public class QuestUpdateService {

    private final QuestMapper questMapper;

    @Transactional
    public Long completeQuest(Long userId, Long questId) {
        QuestVO quest = questMapper.findById(questId);

        if (quest == null) {
            throw ApplicationException.from(QuestErrorCode.QUEST_NOT_FOUND);
        }

        if (!quest.getUserId().equals(userId)) {
            throw ApplicationException.from(QuestErrorCode.QUEST_USER_ID_MISMATCH);
        }

        QuestStatus newStatus = (quest.getQuestStatus() == QuestStatus.COMPLETED)
            ? QuestStatus.NOT_COMPLETED
            : QuestStatus.COMPLETED;

        questMapper.updateStatus(questId, newStatus);

        return questId;
    }
}
