package com.talented.buttie.user.dto.response;

import com.talented.buttie.user.domain.UserProfileVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;

@ApiModel(description = "회원 프로필 조회 응답")
public record GetUserProfileResponseDTO(

    @ApiModelProperty(value = "닉네임", example = "김재준")
    String nickname,

    @ApiModelProperty(value = "버티 누적 경험치", example = "340")
    Integer buttieTotalExp,

    @ApiModelProperty(value = "버티 레벨", example = "5")
    Integer buttieLevel,

    @ApiModelProperty(value = "이름", example = "김재준")
    String username,

    @ApiModelProperty(value = "생년월일", example = "1998-03-15")
    LocalDate birthDate,

    @ApiModelProperty(value = "휴대전화 번호", example = "010-1234-5678")
    String phoneNumber,

    @ApiModelProperty(value = "이메일", example = "skadngus1128@gmail.com")
    String email
) {
    public static GetUserProfileResponseDTO from(UserProfileVO vo) {
        return new GetUserProfileResponseDTO(
            vo.getNickname(),
            vo.getButtieTotalExp(),
            vo.getButtieLevel(),
            vo.getUsername(),
            vo.getBirthDate(),
            vo.getPhoneNumber(),
            vo.getEmail()
        );
    }
}