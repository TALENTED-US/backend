package com.talented.buttie.user.dto.response;

import com.talented.buttie.user.domain.UserProfileVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;

@ApiModel(description = "회원 프로필 조회 응답")
public record GetUserProfileResponseDTO(

<<<<<<< HEAD
    @ApiModelProperty(value = "닉네임", example = "킴킴킴")
=======
    @ApiModelProperty(value = "닉네임", example = "김재준")
>>>>>>> origin/develop
    String userNickname,

    @ApiModelProperty(value = "버티 누적 경험치", example = "340")
    Integer buttieTotalExp,

    @ApiModelProperty(value = "버티 레벨", example = "5")
    Integer buttieLevel,

<<<<<<< HEAD
    @ApiModelProperty(value = "이름", example = "김민서")
    String userName,

    @ApiModelProperty(value = "생년월일", example = "2026-07-30")
=======
    @ApiModelProperty(value = "이름", example = "홍길동")
    String userName,

    @ApiModelProperty(value = "생년월일", example = "2002-03-29")
>>>>>>> origin/develop
    LocalDate birthDate,

    @ApiModelProperty(value = "휴대전화 번호", example = "010-1234-5678")
    String userPhoneNumber,

<<<<<<< HEAD
    @ApiModelProperty(value = "이메일", example = "skadngus1128@gmail.com")
=======
    @ApiModelProperty(value = "이메일", example = "qwert1234@gmail.com")
>>>>>>> origin/develop
    String userEmail
) {
    public static GetUserProfileResponseDTO from(UserProfileVO vo) {
        return new GetUserProfileResponseDTO(
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