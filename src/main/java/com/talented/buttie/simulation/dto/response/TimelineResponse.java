package com.talented.buttie.simulation.dto.response;

import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.simulation.domain.TimelineVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.Builder;

@ApiModel(description = "타임라인 생성 응답")
@Builder
public record TimelineResponse(
    @ApiModelProperty(value = "암호화된 시뮬레이션 ID", example = "exp123...")
    @NotBlank
    String simulationId,

    @ApiModelProperty(value = "암호화된 사용자 ID", example = "exp123...")
    @NotBlank
    String userId,

    @ApiModelProperty(value = "현재 버티는 기간", example = "2.6")
    @NotNull
    BigDecimal currentPrepMonths,

    @ApiModelProperty(value = "예상 버티는 기간", example = "8.2")
    @NotNull
    BigDecimal expectPrepMonths,

    @ApiModelProperty(value = "목표 취업일", example = "2027-01-01")
    LocalDate targetEmploymentDate,

    @ApiModelProperty(value = "위험 잔액", example = "500000")
    @NotNull
    @Positive(message = "위험 금액은 0이상이어야 합니다.")
    Integer livingFundThreshold
) {
    public static TimelineResponse from(TimelineVO vo){
        return TimelineResponse.builder()
            .simulationId(PKCrypto.encrypt(vo.getSimulationId()))
            .userId(PKCrypto.encrypt(vo.getUserId()))
            .currentPrepMonths(vo.getCurrentPrepMonths())
            .expectPrepMonths(vo.getExpectPrepMonths())
            .targetEmploymentDate(vo.getTargetEmploymentDate())
            .livingFundThreshold(vo.getLivingFundThreshold())
            .build();
    }
}
