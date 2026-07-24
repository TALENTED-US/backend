package com.talented.buttie.simulation.dto.request;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record CreateSimulationRequestDTO(
    LocalDateTime startDate,
    LocalDateTime endDate
) {
}
