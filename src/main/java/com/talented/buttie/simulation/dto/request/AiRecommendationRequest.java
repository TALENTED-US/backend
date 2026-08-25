package com.talented.buttie.simulation.dto.request;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiRecommendationRequest {

    @NotBlank(message = "추천을 위한 요청 문구를 입력해주세요.")
    private String prompt;
}
