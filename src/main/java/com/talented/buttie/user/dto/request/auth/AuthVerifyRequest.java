package com.talented.buttie.user.dto.request.auth;

import javax.validation.constraints.NotBlank;

public record AuthVerifyRequest(
    @NotBlank(message = "본인인증 ID는 필수입니다.")
    String identityVerificationId
) {

}
