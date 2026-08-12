package com.talented.buttie.mydata.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.mydata.client.MydataApiClient;
import com.talented.buttie.mydata.client.dto.MydataAuthorizationResponse;
import com.talented.buttie.mydata.domain.MydataConnectionVO;
import com.talented.buttie.mydata.exception.MydataErrorCode;
import com.talented.buttie.mydata.mapper.MydataConnectionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MydataConnectionService {

    private static final String MOCK_PROVIDER = "MOCK";

    private final MydataApiClient mydataApiClient;
    private final MydataTokenService mydataTokenService;
    private final MydataConnectionMapper mydataConnectionMapper;

    public MydataConnectionVO connect(Long userId) {
        MydataConnectionVO existing = mydataConnectionMapper.findByUserIdAndProvider(
            userId,
            MOCK_PROVIDER
        );
        if (existing != null) {
            throw ApplicationException.from(MydataErrorCode.MYDATA_ALREADY_CONNECTED);
        }

        MydataAuthorizationResponse authorization = mydataApiClient.authorize(userId);
        MydataConnectionVO connection = mydataTokenService.issueConnection(
            userId,
            MOCK_PROVIDER,
            authorization.getCode()
        );

        if (mydataConnectionMapper.insert(connection) != 1) {
            throw ApplicationException.from(MydataErrorCode.MYDATA_CONNECTION_FAILED);
        }
        return connection;
    }
}
