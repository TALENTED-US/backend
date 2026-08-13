package com.talented.buttie.mydata.service.mydata;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.mydata.domain.MydataAssetType;
import com.talented.buttie.mydata.dto.response.MydataAssetDeletionResponse;
import com.talented.buttie.mydata.exception.MydataErrorCode;
import com.talented.buttie.mydata.mapper.AccountMapper;
import com.talented.buttie.mydata.mapper.CardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MydataAssetDeleteService {

    private final MydataConnectionValidator mydataConnectionValidator;
    private final AccountMapper accountMapper;
    private final CardMapper cardMapper;

    @Transactional
    public MydataAssetDeletionResponse deactivate(
        Long userId,
        MydataAssetType assetType,
        String externalAssetId
    ) {
        mydataConnectionValidator.validateConnected(userId);
        int affectedRows = switch (assetType) {
            case ACCOUNT -> accountMapper.deactivateByUserIdAndExternalId(
                userId,
                externalAssetId
            );
            case CARD -> cardMapper.deactivateByUserIdAndExternalId(userId, externalAssetId);
        };
        if (affectedRows != 1) {
            throw ApplicationException.from(MydataErrorCode.MYDATA_ASSET_NOT_FOUND);
        }
        return new MydataAssetDeletionResponse(assetType, externalAssetId, true);
    }
}
