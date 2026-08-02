package com.talented.buttie.user.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.user.domain.UserProfileVO;
import com.talented.buttie.user.dto.response.GetUserProfileResponseDTO;
import com.talented.buttie.user.service.UserService;
import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.user.domain.EmploymentPreparationVO;
import com.talented.buttie.user.dto.request.UpdateEmploymentPreparationRequestDTO;
import com.talented.buttie.user.dto.response.UserPKResponseDTO;
import com.talented.buttie.common.security.annotation.AuthUser;
import com.talented.buttie.common.security.AuthenticationUser;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ApplicationResponse<UserPKResponseDTO> saveEmploymentPreparation(
        @AuthUser AuthenticationUser user,
        @Valid @RequestBody UpdateEmploymentPreparationRequestDTO request
    ) {
        Long targetUserId = user.userId();
        EmploymentPreparationVO employmentPreparation = userService.saveEmploymentPreparation(targetUserId, request);
        return ApplicationResponse.onSuccess(new UserPKResponseDTO(PKCrypto.encrypt(employmentPreparation.getUserId())));
    }

    @GetMapping
    @ApiOperation("회원 프로필 조회")
    public ApplicationResponse<GetUserProfileResponseDTO> getUserProfile(
        @AuthUser AuthenticationUser user
    ) {
        Long targetUserId = user.userId();
        UserProfileVO userProfile = userService.getUserProfile(targetUserId);
        return ApplicationResponse.onSuccess(GetUserProfileResponseDTO.from(userProfile));
    }
}