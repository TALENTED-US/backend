package com.talented.buttie.user.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.talented.buttie.mydata.domain.ConnectionStatus;
import com.talented.buttie.user.domain.EmploymentPreparationType;
import com.talented.buttie.user.domain.MyProfileSummaryVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;

@ApiModel(description = "마이페이지 상단 프로필 조회 응답")
public record GetMyProfileSummaryResponse(

    @ApiModelProperty(value = "버티 이미지 URL", example = "https://cdn.buttie.com/buttie/lv1_stable.png", required = true)
    String buttieImageUrl,

    @ApiModelProperty(value = "버티 레벨", example = "1", required = true)
    Integer buttieLevel,

    @ApiModelProperty(value = "누적 경험치", example = "5", required = true)
    Integer buttieTotalExp,

    @ApiModelProperty(value = "목표 경험치", example = "50", required = true)
    Integer requiredExp,

    @ApiModelProperty(value = "재정 위험 수준", example = "STABLE", required = true)
    String riskLevel,

    @ApiModelProperty(value = "닉네임", example = "닉넴뭐하지", required = true)
    String userNickname,

    @ApiModelProperty(value = "이메일", example = "exodus123@email.com", required = true)
    String userEmail,

    @ApiModelProperty(value = "취업 준비 유형", example = "REEMPLOYMENT", required = true)
    EmploymentPreparationType employmentPrepType,

    @ApiModelProperty(value = "준비 시작일", example = "2026-09-01", required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate prepStartDate,

    @ApiModelProperty(value = "목표 취업 시점", example = "2126-09-01", required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate targetEmploymentDate,

    @ApiModelProperty(value = "마이데이터 연결 상태", example = "CONNECTED", required = true)
    ConnectionStatus mydataStatus,

    @ApiModelProperty(value = "마이데이터 마지막 갱신 시각", example = "2026-07-29 09:12:00", required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime lastSyncedAt
) {
    public static GetMyProfileSummaryResponse from(MyProfileSummaryVO vo) {
        return new GetMyProfileSummaryResponse(
            vo.getButtieImageUrl(),
            vo.getButtieLevel(),
            vo.getButtieTotalExp(),
            vo.getRequiredExp(),
            vo.getRiskLevel(),
            vo.getUserNickname(),
            vo.getUserEmail(),
            vo.getEmploymentPrepType(),
            vo.getPrepStartDate(),
            vo.getTargetEmploymentDate(),
            vo.getMydataStatus(),
            vo.getLastSyncedAt()
        );
    }
}
