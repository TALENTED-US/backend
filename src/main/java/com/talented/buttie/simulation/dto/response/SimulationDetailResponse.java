package com.talented.buttie.simulation.dto.response;

import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.simulation.domain.SimulationVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

@ApiModel(description = "시뮬레이션 통합 조회 응답")
@Builder
public record SimulationDetailResponse(

    @ApiModelProperty(value = "암호화된 시뮬레이션 ID")
    String simulationId,

    @ApiModelProperty(value = "암호화된 사용자 ID")
    String userId,

    @ApiModelProperty(value = "암호화된 기준 스냅샷 ID")
    String snapshotId,

    @ApiModelProperty(value = "시뮬레이션 수행 시작일", example = "2026-08-01")
    LocalDate simulationStartDate,

    @ApiModelProperty(value = "시뮬레이션 수행 종료일", example = "2027-01-31")
    LocalDate simulationDueDate,

    @ApiModelProperty(value = "종료 예상 잔액", example = "5000000")
    Integer simulationEndAmount,

    @ApiModelProperty(value = "예상 버티는 기간", example = "8.25")
    BigDecimal expectPrepMonths,

    @ApiModelProperty(value = "현재 현금흐름 유지 시 자금이 고갈되지 않는지 여부")
    Boolean sustainable,

    @ApiModelProperty(value = "관련 월별 재정 계획 리스트")
    List<MonthlyProjectionResponse> monthlyProjections
) {
    public static SimulationDetailResponse from(SimulationVO simulation){
        return SimulationDetailResponse.builder()
            .simulationId(PKCrypto.encrypt(simulation.getSimulationId()))
            .userId(PKCrypto.encrypt(simulation.getUserId()))
            .snapshotId(PKCrypto.encrypt(simulation.getSnapshotId()))
            .simulationStartDate(simulation.getSimulationStartDate())
            .simulationDueDate(simulation.getSimulationDueDate())
            .simulationEndAmount(simulation.getSimulationEndAmount())
            .expectPrepMonths(simulation.getExpectPrepMonths())
            .sustainable(isSustainable(simulation.getExpectPrepMonths()))
            .monthlyProjections(
                simulation.getMonthlyProjections().stream()
                    .map(MonthlyProjectionResponse::from)
                    .toList()
            )
            .build();
    }

    private static boolean isSustainable(BigDecimal prepMonths) {
        return prepMonths != null && prepMonths.compareTo(new BigDecimal("999.99")) == 0;
    }
}
