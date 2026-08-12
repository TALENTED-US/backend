package com.talented.buttie.mydata.dto.response;

import lombok.Builder;

@Builder
public record MydataAssetRegistrationResponse(
    int registeredAccountCount,
    int registeredCardCount,
    int insertedTransactionCount,
    int skippedTransactionCount,
    int excludedDuplicateCount,
    int fixedExpenseCandidateCount
) {
}
