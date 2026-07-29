package com.talented.buttie.log.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogVO {

    private Long logId;
    private Long userId;
    private String entityType;
    private Long entityId;
    private String action;
    private String logDetail;
    private LocalDateTime logCreatedAt;
}
