package com.talented.buttie.simulation.dto.response;

import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.simulation.domain.SimulationVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@ApiModel(description = "최근 확정 시뮬레이션 조회 응답")
@Builder
public record ConfirmedSimulationResponse(

    @ApiModelProperty(value = "시뮬레이션 ID", example = "1")
    Long simulationId,

    @ApiModelProperty(value = "암호화된 사용자 ID", example = "qwe123...")
    String userId,

    @ApiModelProperty(value = "기준 스냅샷 ID", example = "10")
    Long snapshotId,

    @ApiModelProperty(value = "시뮬레이션 수행 시작일", example = "2026-08-01")
    LocalDate simulationStartDate,

    @ApiModelProperty(value = "시뮬레이션 종료일", example = "2027-01-31")
    LocalDate simulationDueDate,

    @ApiModelProperty(value = "종료 예상 잔액", example = "5000000")
    Integer simulationEndAmount,

    @ApiModelProperty(value = "준비 가능 개월", example = "8.25")
    BigDecimal prepMonths,

    @ApiModelProperty(value = "확정 일시", example = "2027-02-01T12:00:00")
    LocalDateTime confirmedAt,

    @ApiModelProperty(value = "관련 월별 재정 계획 리스트")
    List<MonthlyProjectionResponse> monthlyProjections
) {
    public static ConfirmedSimulationResponse from(
        SimulationVO simulation
    ) {
        return ConfirmedSimulationResponse.builder()
            .simulationId(simulation.getSimulationId())
            .userId(PKCrypto.encrypt(simulation.getUserId()))
            .snapshotId(simulation.getSnapshotId())
            .simulationStartDate(simulation.getSimulationStartDate())
            .simulationDueDate(simulation.getSimulationDueDate())
            .simulationEndAmount(simulation.getSimulationEndAmount())
            .prepMonths(simulation.getPrepMonths())
            .confirmedAt(simulation.getConfirmedAt())
            .monthlyProjections(
                simulation.getMonthlyProjections().stream()
                    .map(MonthlyProjectionResponse::from)
                    .toList()
            )
            .build();
    }
}
