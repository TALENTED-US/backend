package com.talented.buttie.mydata.service.mydata;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.mydata.client.MydataApiClient;
import com.talented.buttie.mydata.client.dto.MydataAuthorizationResponse;
import com.talented.buttie.mydata.domain.ConnectionStatus;
import com.talented.buttie.mydata.domain.MydataConnectionVO;
import com.talented.buttie.mydata.exception.MydataErrorCode;
import com.talented.buttie.mydata.mapper.MydataConnectionMapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MydataConnectionService {

    private static final String MOCK_PROVIDER = "MOCK";

    private final MydataApiClient mydataApiClient;
    private final MydataTokenService mydataTokenService;
    private final MydataConnectionMapper mydataConnectionMapper;

    @Transactional
    public MydataConnectionVO connect(Long userId) {
        MydataConnectionVO existing = mydataConnectionMapper.findByUserIdAndProvider(
            userId,
            MOCK_PROVIDER
        );
        if (isValidConnection(existing)) {
            throw ApplicationException.from(MydataErrorCode.MYDATA_ALREADY_CONNECTED);
        }

        MydataAuthorizationResponse authorization = mydataApiClient.authorize(userId);
        MydataConnectionVO connection = mydataTokenService.issueConnection(
            userId,
            MOCK_PROVIDER,
            authorization.getCode()
        );

        int affectedRows;
        if (existing == null) {
            affectedRows = mydataConnectionMapper.insert(connection);
        } else {
            connection.setMydataId(existing.getMydataId());
            affectedRows = mydataConnectionMapper.update(connection);
        }

        if (affectedRows != 1) {
            throw ApplicationException.from(MydataErrorCode.MYDATA_CONNECTION_FAILED);
        }
        return connection;
    }

    private boolean isValidConnection(MydataConnectionVO connection) {
        return connection != null
            && connection.getMydataStatus() == ConnectionStatus.CONNECTED
            && connection.getRefreshTokenEncrypted() != null
            && !connection.getRefreshTokenEncrypted().isBlank()
            && connection.getRefreshTokenExpiresAt() != null
            && connection.getRefreshTokenExpiresAt().isAfter(LocalDateTime.now());
    }
}
