package com.talented.buttie.user.dto.request.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Builder;


@Builder
@ApiModel(description = "회원 가입 요청")
public record AuthSignUpRequestDTO(

    @ApiModelProperty(value = "이름", example = "장예연", required = true)
    @NotBlank(message = "이름은 필수입니다.")
    String userName,

    @ApiModelProperty(value = "생년월일", example = "2002-03-29", required = true)
    @NotBlank(message = "생년월일은 필수입니다.")
    @Pattern(
        regexp = "^\\d{4}-\\d{2}-\\d{2}$",
        message = "생년월일은 yyyy-MM-dd 형식이어야 합니다."
    )
    String birthDate,

    @ApiModelProperty(value = "전화번호", example = "010-1234-5678", required = true)
    @NotBlank(message = "전화번호는 필수입니다.")
    String phoneNumber,

    @ApiModelProperty(value = "이메일", example = "abc123@example.com", required = true)
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    String userEmail,

    @ApiModelProperty(value = "비밀번호", example = "Password123!", required = true)
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,20}$",
        message = "비밀번호는 영문, 숫자, 특수문자를 포함한 8~20자여야 합니다."
    )
    String userPassword,

    @ApiModelProperty(value = "비밀번호 확인", example = "Password123!", required = true)
    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    String userPasswordCheck,

    @ApiModelProperty(value = "닉네임", example = "버티", required = true)
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자여야 합니다.")
    String userNickname
) {


}
