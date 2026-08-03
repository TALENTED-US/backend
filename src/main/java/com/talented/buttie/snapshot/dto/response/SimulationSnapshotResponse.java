package com.talented.buttie.snapshot.dto.response;

import java.math.BigDecimal;

<<<<<<<< HEAD:src/main/java/com/talented/buttie/snapshot/dto/response/SimulationSnapshotResponse.java
public record SimulationSnapshotResponse(
========
public record SimulationSnapshotResult(
>>>>>>>> 80ffd7f (BUT-62 Fix:DTO 접미사 삭제 #77):src/main/java/com/talented/buttie/snapshot/dto/response/SimulationSnapshotResult.java
    Long snapshotId,
    Long userId,
    Integer liquidAssets,
    BigDecimal targetAchievementRate,
    BigDecimal prepPossibleMonths
) {
}
