package com.talented.buttie.user.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.talented.buttie.user.domain.UserProfileVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;

@ApiModel(description = "회원 프로필 조회 응답")
public record GetUserProfileResponseDTO(

    @ApiModelProperty(value = "버티 이미지 URL", example = "https://cdn.buttie.com/buttie/lv1_stable.png")
    String buttieImageUrl,

    @ApiModelProperty(value = "버티 레벨", example = "5")
    Integer buttieLevel,

    @ApiModelProperty(value = "닉네임", example = "김재준")
    String userNickname,

    @ApiModelProperty(value = "이름", example = "김민서")
    String userName,

    @ApiModelProperty(value = "생년월일", example = "1998-03-15")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate birthDate,

    @ApiModelProperty(value = "휴대전화 번호", example = "010-1234-5678")
    String userPhoneNumber,

    @ApiModelProperty(value = "이메일", example = "qwert1234@gmail.com")
    String userEmail
) {
    public static GetUserProfileResponseDTO from(UserProfileVO vo) {
        return new GetUserProfileResponseDTO(
            vo.getButtieImageUrl(),
            vo.getButtieLevel(),
            vo.getUserNickname(),
            vo.getUserName(),
            vo.getBirthDate(),
            vo.getUserPhoneNumber(),
            vo.getUserEmail()
        );
    }
}