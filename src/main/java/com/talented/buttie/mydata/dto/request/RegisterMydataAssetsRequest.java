package com.talented.buttie.mydata.dto.request;

import java.util.List;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

public record RegisterMydataAssetsRequest(
    @NotNull(message = "계좌 선택 목록은 필수입니다.")
    List<String> accountIds,
    @NotNull(message = "카드 선택 목록은 필수입니다.")
    List<String> cardIds
) {

    @AssertTrue(message = "계좌 또는 카드를 하나 이상 선택해야 합니다.")
    public boolean isAssetSelected() {
        return accountIds != null && cardIds != null
            && (!accountIds.isEmpty() || !cardIds.isEmpty());
    }
}
