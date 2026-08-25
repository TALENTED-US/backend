package com.talented.buttie.mydata.dto.response;

import com.talented.buttie.mydata.domain.ConnectionStatus;
import com.talented.buttie.mydata.domain.MydataConnectionVO;
import java.time.LocalDateTime;

public record MydataConnectionResponse(
    Long mydataId,
    String provider,
    ConnectionStatus status,
    LocalDateTime refreshTokenExpiresAt
) {

    public static MydataConnectionResponse from(MydataConnectionVO connection) {
        return new MydataConnectionResponse(
            connection.getMydataId(),
            connection.getProvider(),
            connection.getMydataStatus(),
            connection.getRefreshTokenExpiresAt()
        );
    }
}
