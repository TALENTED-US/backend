package com.talented.buttie.user.dto.response.user;

import com.talented.buttie.user.domain.UserProfileVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;

@ApiModel(description = "회원 프로필 조회 응답")
public record GetUserProfileResponse(


    @ApiModelProperty(value = "닉네임", example = "김재준")
    String userNickname,

    @ApiModelProperty(value = "버티 누적 경험치", example = "340")
    Integer buttieTotalExp,

    @ApiModelProperty(value = "버티 레벨", example = "5")
    Integer buttieLevel,

    @ApiModelProperty(value = "이름", example = "김민서")
    String userName,

    @ApiModelProperty(value = "생년월일", example = "2026-07-30")
    LocalDate birthDate,

    @ApiModelProperty(value = "휴대전화 번호", example = "010-1234-5678")
    String userPhoneNumber,

    @ApiModelProperty(value = "이메일", example = "qwert1234@gmail.com")

    String userEmail
) {
    public static GetUserProfileResponse from(UserProfileVO vo) {
        return new GetUserProfileResponse(
            vo.getUserNickname(),
            vo.getButtieTotalExp(),
            vo.getButtieLevel(),
            vo.getUserName(),
            vo.getBirthDate(),
            vo.getUserPhoneNumber(),
            vo.getUserEmail()
        );
    }
}