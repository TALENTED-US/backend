package com.talented.buttie.snapshot.dto.response;

import java.math.BigDecimal;

public record SimulationSnapshotResponse(
    Long snapshotId,
    Long userId,
    Integer liquidAssets,
    BigDecimal targetAchievementRate,
    BigDecimal currentPrepMonths,
    Boolean sustainable
) {
}
