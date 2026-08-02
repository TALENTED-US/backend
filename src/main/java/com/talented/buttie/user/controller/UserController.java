package com.talented.buttie.user.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.user.dto.request.UpdateEmploymentPreparationRequestDTO;
import com.talented.buttie.user.dto.response.UserPKResponseDTO;
import com.talented.buttie.user.service.UserService;
import com.talented.buttie.common.security.annotation.AuthUser;
import com.talented.buttie.common.security.AuthenticationUser;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import javax.validation.Valid;
@Api(tags = "User")
@RestController
@RequestMapping("/api/users/my")
@RequiredArgsConstructor

public class UserController {

    private final UserService userService;

    @PatchMapping("/employment-preparation")
    @ApiOperation("취업 준비 정보 수정")
    public ApplicationResponse<UserPKResponseDTO> modifyEmploymentPreparation(
        @AuthUser AuthenticationUser user,
        @Valid @RequestBody UpdateEmploymentPreparationRequestDTO request
    ) {
        Long targetUserId = user.userId();
        Long userId = userService.modifyEmploymentPreparation(targetUserId, request);
        return ApplicationResponse.onSuccess(new UserPKResponseDTO(PKCrypto.encrypt(userId)));
    }
}