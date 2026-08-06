package com.talented.buttie.simulation.dto.response;

import com.talented.buttie.common.util.PKCrypto;
import io.swagger.annotations.ApiModelProperty;

public record ApplySimulationItemResponse(
    @ApiModelProperty(value = "암호화된 적용 항목 ID")
    String itemId
) {
    public static ApplySimulationItemResponse from(Long itemId) {
        return new ApplySimulationItemResponse(PKCrypto.encrypt(itemId));
    }
}
