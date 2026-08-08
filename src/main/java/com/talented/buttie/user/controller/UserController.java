package com.talented.buttie.user.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.user.domain.UserProfileVO;
import com.talented.buttie.user.dto.response.GetUserProfileResponseDTO;
import com.talented.buttie.user.service.UserService;
import com.talented.buttie.common.security.AuthenticationUser;
import com.talented.buttie.common.security.annotation.AuthUser;
import com.talented.buttie.user.domain.EmploymentPreparationVO;
import com.talented.buttie.user.dto.request.ModifyUserProfileRequestDTO;
import com.talented.buttie.user.dto.request.UpdateEmploymentPreparationRequestDTO;
import com.talented.buttie.user.dto.response.UserPKResponseDTO;
import com.talented.buttie.user.dto.request.CreateEmploymentPreparationRequestDTO;
import org.springframework.web.bind.annotation.PostMapping;
import com.talented.buttie.user.dto.response.GetEmploymentPreparationResponseDTO;
import com.talented.buttie.user.dto.request.WithdrawUserRequestDTO;
import com.talented.buttie.user.domain.ButiDashboardVO;
import com.talented.buttie.user.dto.response.GetButiDashboardResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
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


    @PostMapping("/employment-preparation")
    @ApiOperation("취업 준비 정보 등록")
    public ApplicationResponse<UserPKResponseDTO> createEmploymentPreparation(
        @AuthUser AuthenticationUser user,
        @Valid @RequestBody CreateEmploymentPreparationRequestDTO request
    ) {
        Long targetUserId = user.userId();
        Long userId = userService.createEmploymentPreparation(targetUserId, request);
        return ApplicationResponse.onSuccess(new UserPKResponseDTO(PKCrypto.encrypt(userId)));
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

    @GetMapping("/buti")
    @ApiOperation("버티 성장 대시보드 조회")
    public ApplicationResponse<GetButiDashboardResponse> getButiDashboard(
        @AuthUser AuthenticationUser user
    ) {
        Long targetUserId = user.userId();
        ButiDashboardVO butiDashboard = userService.getButiDashboard(targetUserId);
        return ApplicationResponse.onSuccess(GetButiDashboardResponse.from(butiDashboard));
    }

    @GetMapping("/employment-preparation")
    @ApiOperation("취업 준비 정보 조회")
    public ApplicationResponse<GetEmploymentPreparationResponseDTO> getEmploymentPreparation(
        @AuthUser AuthenticationUser user
    ){
        Long targetUserId = user.userId();
        EmploymentPreparationVO employmentPreparation = userService.getEmploymentPreparation(targetUserId);
        return ApplicationResponse.onSuccess(GetEmploymentPreparationResponseDTO.from(employmentPreparation));
    }

    @PatchMapping
    @ApiOperation("회원 프로필 수정")
    public ApplicationResponse<UserPKResponseDTO> modifyUserProfile(
        @AuthUser AuthenticationUser user,
        @Valid @RequestBody ModifyUserProfileRequestDTO request
    ) {
        Long targetUserId = user.userId();
        Long modifiedUserId = userService.modifyUserProfile(targetUserId, request);
        return ApplicationResponse.onSuccess(new UserPKResponseDTO(PKCrypto.encrypt(modifiedUserId)));
    }

    @DeleteMapping
    @ApiOperation("회원 탈퇴")
    public ApplicationResponse<UserPKResponseDTO> withdrawUser(
        @AuthUser AuthenticationUser user,
        @Valid @RequestBody WithdrawUserRequestDTO request
    ) {
        Long targetUserId = user.userId();
        Long withdrawnUserId = userService.withdrawUser(targetUserId, request);
        return ApplicationResponse.onSuccess(new UserPKResponseDTO(PKCrypto.encrypt(withdrawnUserId)));
    }
}