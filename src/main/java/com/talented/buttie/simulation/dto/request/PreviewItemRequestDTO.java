package com.talented.buttie.simulation.dto.request;

import com.talented.buttie.simulation.domain.SimulationItemCategory;
import com.talented.buttie.simulation.domain.SimulationRecurrenceType;
import io.swagger.annotations.ApiModel;
import java.time.LocalDate;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import lombok.Builder;

@ApiModel("시뮬레이션 항목 대입 미리보기")
@Builder
public record PreviewItemRequestDTO(
    @NotNull SimulationItemCategory simulationItemCategory,
    @NotBlank String itemName,
    @PositiveOrZero Integer amount,
    @NotNull LocalDate applyStartDate,
    LocalDate applyEndDate,
    @NotNull SimulationRecurrenceType recurrenceType,
    Integer recurrenceDay,
    Long policyId,
    String detailValue
    ) {
}
