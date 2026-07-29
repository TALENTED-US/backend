package com.talented.buttie.quest.domain;

import java.time.LocalDateTime;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class QuestVO {
    private Long questId;
    private Long userId;
    private Long simulationId;
    private Long simulationItemId;
    private Long transactionId;
    private QuestType questType;
    private String questTitle;
    private String questDescription;
    private LocalDateTime questDeadline;
    private QuestStatus questStatus;
    private String questUrl;
    private Integer expReward;
    private LocalDateTime questCompletedAt;
}
