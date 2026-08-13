package com.talented.buttie.simulation.dto.response.snapshot;

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
