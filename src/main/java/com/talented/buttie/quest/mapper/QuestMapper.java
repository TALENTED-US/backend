package com.talented.buttie.quest.mapper;

import com.talented.buttie.quest.domain.QuestStatus;
import com.talented.buttie.quest.domain.QuestVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface QuestMapper {

    Long save(QuestVO quest);

    List<QuestVO> findAllByUserId(@Param("userId") Long userId);

    List<QuestVO> findAllBySimulationId(@Param("simulationId") Long simulationId);

    QuestVO findById(@Param("questId") Long questId);

    int updateStatus(
        @Param("questId") Long questId,
        @Param("questStatus") QuestStatus questStatus
    );

    List<QuestVO> findAllBySimulationIdForUpdate(@Param("simulationId") Long simulationId);

    int revertCompletedStatus(@Param("questId") Long questId);

    int completeQuestStatus(@Param("questId") Long questId);
}
