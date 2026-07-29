package com.talented.buttie.quest.domain;

import java.time.LocalDateTime;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ButtieLevelVO {
    private Integer level;
    private String stageName;
    private Integer requiredExp;
    private String levelDescription;
    private String imageUrlStable;
    private String imageUrlCaution;
    private String imageUrlDanger;
    private LocalDateTime levelCreatedAt;
    private LocalDateTime levelUpdatedAt;
}
