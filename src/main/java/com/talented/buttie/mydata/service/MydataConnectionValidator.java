package com.talented.buttie.mydata.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.mydata.domain.ConnectionStatus;
import com.talented.buttie.mydata.domain.MydataConnectionVO;
import com.talented.buttie.mydata.exception.MydataErrorCode;
import com.talented.buttie.mydata.mapper.MydataConnectionMapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MydataConnectionValidator {

    private static final String MOCK_PROVIDER = "MOCK";

    private final MydataConnectionMapper mydataConnectionMapper;

    public void validateConnected(Long userId) {
        MydataConnectionVO connection = mydataConnectionMapper.findByUserIdAndProvider(
            userId,
            MOCK_PROVIDER
        );

        if (connection == null
            || connection.getMydataStatus() != ConnectionStatus.CONNECTED
            || connection.getRefreshTokenEncrypted() == null
            || connection.getRefreshTokenEncrypted().isBlank()
            || connection.getRefreshTokenExpiresAt() == null
            || !connection.getRefreshTokenExpiresAt().isAfter(LocalDateTime.now())) {
            throw ApplicationException.from(MydataErrorCode.MYDATA_NOT_CONNECTED);
        }
    }
}
