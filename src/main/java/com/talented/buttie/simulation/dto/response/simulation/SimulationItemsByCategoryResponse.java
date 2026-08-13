package com.talented.buttie.simulation.dto.response.simulation;

import io.swagger.annotations.ApiModelProperty;
import java.util.List;

public record SimulationItemsByCategoryResponse(

    @ApiModelProperty(value = "카테고리별 적용된 항목 리스트")
    List<SimulationItemResponse> appliedItems
) {

}
