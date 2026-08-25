package com.talented.buttie.simulation.dto.response.simulation;

import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.simulation.domain.SimulationVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@ApiModel("최근 확정 시뮬레이션 조회 응답")
@Builder
public record ConfirmedSimulationResponse(

    @ApiModelProperty(value = "암호화된 시뮬레이션 ID")
    String simulationId,

    @ApiModelProperty(value = "암호화된 사용자 ID")
    String userId,

    @ApiModelProperty(value = "시뮬레이션 수행 시작일", example = "2026-08-01")
    LocalDate simulationStartDate,

    @ApiModelProperty(value = "시뮬레이션 수행 종료일", example = "2027-01-31")
    LocalDate simulationDueDate,

    @ApiModelProperty(value = "종료 예상 잔액", example = "5000000")
    Integer simulationEndAmount,

    @ApiModelProperty(value = "현재 버티는 기간", example = "5.12")
    BigDecimal currentPrepMonths,

    @ApiModelProperty(value = "예상 버티는 기간", example = "8.25")
    BigDecimal expectPrepMonths,

    @ApiModelProperty(value = "확정 일시", example = "2027-02-01T12:00:00")
    LocalDateTime confirmedAt,

    @ApiModelProperty(value = "관련 월별 재정 계획 리스트")
    List<MonthlyProjectionResponse> monthlyProjections,

    @ApiModelProperty(value = "적용된 시뮬레이션 항목 리스트")
    List<SimulationItemResponse> appliedItems
) {

    public static ConfirmedSimulationResponse from(
        SimulationVO simulation,
        BigDecimal currentPrepMonths,
        List<SimulationItemResponse> appliedItems
    ) {
        return ConfirmedSimulationResponse.builder()
            .simulationId(PKCrypto.encrypt(simulation.getSimulationId()))
            .userId(PKCrypto.encrypt(simulation.getUserId()))
            .simulationStartDate(simulation.getSimulationStartDate())
            .simulationDueDate(simulation.getSimulationDueDate())
            .simulationEndAmount(simulation.getSimulationEndAmount())
            .currentPrepMonths(currentPrepMonths)
            .expectPrepMonths(simulation.getExpectPrepMonths())
            .confirmedAt(simulation.getConfirmedAt())
            .monthlyProjections(
                simulation.getMonthlyProjections().stream()
                    .map(MonthlyProjectionResponse::from)
                    .toList()
            )
            .appliedItems(appliedItems)
            .build();
    }
}
