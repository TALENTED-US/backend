package com.talented.buttie.snapshot.dto.result;

import java.math.BigDecimal;

public record SimulationSnapshotResultDTO(
    Long snapshotId,
    Long userId,
    Integer liquidAssets,
    BigDecimal targetAchievementRate,
    BigDecimal prepPossibleMonths
) {
}
