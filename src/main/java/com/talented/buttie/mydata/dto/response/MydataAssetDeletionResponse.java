package com.talented.buttie.mydata.dto.response;

import com.talented.buttie.mydata.domain.MydataAssetType;

public record MydataAssetDeletionResponse(
    MydataAssetType assetType,
    String assetId,
    boolean disconnected
) {
}
