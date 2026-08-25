package com.talented.buttie.mydata.domain;

import java.time.LocalDateTime;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MydataConnectionVO {
    private Long mydataId;
    private Long userId;
    private String provider;
    @ToString.Exclude
    private String refreshTokenEncrypted;
    private LocalDateTime refreshTokenExpiresAt;
    private ConnectionStatus mydataStatus;
    private LocalDateTime lastSyncedAt;

    public static MydataConnectionVO connected(
        Long userId,
        String provider,
        String encryptedRefreshToken,
        LocalDateTime refreshTokenExpiresAt
    ) {
        return MydataConnectionVO.builder()
            .userId(userId)
            .provider(provider)
            .refreshTokenEncrypted(encryptedRefreshToken)
            .refreshTokenExpiresAt(refreshTokenExpiresAt)
            .mydataStatus(ConnectionStatus.CONNECTED)
            .lastSyncedAt(null)
            .build();
    }
}
