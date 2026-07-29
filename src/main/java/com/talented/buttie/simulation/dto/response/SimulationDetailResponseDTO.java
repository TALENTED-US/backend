package com.talented.buttie.simulation.dto.response;

import com.talented.buttie.simulation.domain.SimulationVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

@ApiModel(description = "시뮬레이션 통합 조회 응답")
@Builder
public record SimulationDetailResponseDTO(

    @ApiModelProperty(value = "시뮬레이션 ID", example = "1")
    Long simulationId,

    @ApiModelProperty(value = "사용자 ID", example = "1")
    Long userId,

    @ApiModelProperty(value = "기준 스냅샷 ID", example = "10")
    Long snapshotId,

    @ApiModelProperty(value = "시뮬레이션 수행 시작일", example = "2026-08-01")
    LocalDate startDate,

    @ApiModelProperty(value = "시뮬레이션 종료일", example = "2027-01-31")
    LocalDate endDate,

    @ApiModelProperty(value = "종료 예상 잔액", example = "5000000")
    Integer endingBalance,

    @ApiModelProperty(value = "준비 가능 개월", example = "8.25")
    BigDecimal prepMonths,

    @ApiModelProperty(value = "관련 월별 재정 계획 리스트")
    List<MonthlyProjectionResponseDTO> monthlyProjections
) {
    public static SimulationDetailResponseDTO from(SimulationVO simulation){
        return SimulationDetailResponseDTO.builder()
            .simulationId(simulation.getSimulationId())
            .userId(simulation.getUserId())
            .snapshotId(simulation.getSnapshotId())
            .startDate(simulation.getSimulationStartDate())
            .endDate(simulation.getSimulationDueDate())
            .endingBalance(simulation.getSimulationEndAmount())
            .prepMonths(simulation.getPrepMonths())
            .monthlyProjections(
                simulation.getMonthlyProjections().stream()
                    .map(MonthlyProjectionResponseDTO::from)
                    .toList()
            )
            .build();
    }
}
