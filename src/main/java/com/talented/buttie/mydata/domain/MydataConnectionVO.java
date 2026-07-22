package com.talented.buttie.mydata.domain;

import java.time.LocalDateTime;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MydataConnectionVO {
    private Long mydataId;
    private Long userId;
    private String provider;
    @ToString.Exclude
    private String providerUserId;
    @ToString.Exclude
    private String refreshTokenEncrypted;
    private LocalDateTime accessTokenExpiresAt;
    private LocalDateTime refreshTokenExpiresAt;
    private String scope;
    private ConnectionStatus status;
    private LocalDateTime lastTokenRefreshedAt;
    private LocalDateTime lastSyncedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
