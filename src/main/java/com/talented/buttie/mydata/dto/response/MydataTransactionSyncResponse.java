package com.talented.buttie.mydata.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record MydataTransactionSyncResponse(
    int insertedTransactionCount,
    int skippedTransactionCount,
    int excludedDuplicateCount,
    int fixedExpenseCandidateCount,
    LocalDateTime lastSyncedAt
) {
}
