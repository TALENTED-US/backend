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
    private String title;
    private String description;
    private LocalDateTime deadline;
    private QuestStatus status;
    private String url;
    private Integer expReward;
    private LocalDateTime completedAt;
}
