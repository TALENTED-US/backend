package com.talented.buttie.quest.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.quest.domain.QuestStatus;
import com.talented.buttie.quest.domain.QuestVO;
import com.talented.buttie.quest.exception.QuestErrorCode;
import com.talented.buttie.quest.mapper.QuestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QuestRevertServiceTest {

    @Mock
    private QuestMapper questMapper;

    @Mock
    private ExperienceService experienceService;

    @InjectMocks
    private QuestRevertService questRevertService;

    private Long userId;
    private Long questId;
    private QuestVO completedQuest;

    @BeforeEach
    void setUp() {
        userId = 1L;
        questId = 100L;
        completedQuest = QuestVO.builder()
            .questId(questId)
            .userId(userId)
            .questStatus(QuestStatus.COMPLETED)
            .expReward(50)
            .build();
    }

    @Test
    @DisplayName("성공: 완료된 퀘스트를 취소하고 지급받은 exp를 회수한다.")
    void revertQuest() {
        given(questMapper.findById(questId)).willReturn(completedQuest);
        given(questMapper.revertCompletedStatus(questId)).willReturn(1);

        assertDoesNotThrow(() -> questRevertService.revertQuest(userId, questId));

        verify(questMapper).revertCompletedStatus(questId);
        verify(experienceService).deductExperience(userId, 50);
    }

    @Test
    @DisplayName("성공: 지급받은 exp가 없으면 회수 없이 취소만 한다.")
    void revertQuestWithoutExpReward() {
        QuestVO quest = QuestVO.builder()
            .questId(questId)
            .userId(userId)
            .questStatus(QuestStatus.COMPLETED)
            .expReward(null)
            .build();
        given(questMapper.findById(questId)).willReturn(quest);
        given(questMapper.revertCompletedStatus(questId)).willReturn(1);

        assertDoesNotThrow(() -> questRevertService.revertQuest(userId, questId));

        verify(experienceService, never()).deductExperience(any(), any());
    }

    @Test
    @DisplayName("실패: questId가 없으면 예외가 발생한다.")
    void throwWhenQuestIdIsNull() {
        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> questRevertService.revertQuest(userId, null)
        );

        assertEquals(QuestErrorCode.QUEST_NOT_FOUND, exception.getCode());
        verifyNoInteractions(questMapper, experienceService);
    }

    @Test
    @DisplayName("실패: 퀘스트가 존재하지 않으면 예외가 발생한다.")
    void throwWhenQuestNotFound() {
        given(questMapper.findById(questId)).willReturn(null);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> questRevertService.revertQuest(userId, questId)
        );

        assertEquals(QuestErrorCode.QUEST_NOT_FOUND, exception.getCode());
        verifyNoInteractions(experienceService);
    }

    @Test
    @DisplayName("실패: 퀘스트 소유자가 다르면 예외가 발생한다.")
    void throwWhenUserMismatch() {
        given(questMapper.findById(questId)).willReturn(completedQuest);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> questRevertService.revertQuest(999L, questId)
        );

        assertEquals(QuestErrorCode.QUEST_USER_ID_MISMATCH, exception.getCode());
        verify(questMapper, never()).revertCompletedStatus(questId);
        verifyNoInteractions(experienceService);
    }

    @Test
    @DisplayName("실패: 완료 상태가 아니면(경합으로 이미 취소/삭제됐으면) 예외가 발생한다.")
    void throwWhenNotCompleted() {
        given(questMapper.findById(questId)).willReturn(completedQuest);
        given(questMapper.revertCompletedStatus(questId)).willReturn(0);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> questRevertService.revertQuest(userId, questId)
        );

        assertEquals(QuestErrorCode.QUEST_NOT_COMPLETED, exception.getCode());
        verifyNoInteractions(experienceService);
    }

    @Test
    @DisplayName("실패: exp 회수 중 예외가 발생하면 QUEST_EXP_REVERT_FAILED를 던진다.")
    void throwWhenExpRevertFails() {
        given(questMapper.findById(questId)).willReturn(completedQuest);
        given(questMapper.revertCompletedStatus(questId)).willReturn(1);
        willThrow(new RuntimeException("db error"))
            .given(experienceService).deductExperience(userId, 50);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> questRevertService.revertQuest(userId, questId)
        );

        assertEquals(QuestErrorCode.QUEST_EXP_REVERT_FAILED, exception.getCode());
    }
}
