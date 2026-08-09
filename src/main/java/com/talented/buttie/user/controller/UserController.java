package com.talented.buttie.user.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.common.security.AuthenticationUser;
import com.talented.buttie.common.security.annotation.AuthUser;
import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.user.domain.ButiDashboardVO;
import com.talented.buttie.user.domain.EmploymentPreparationVO;
import com.talented.buttie.user.domain.UserProfileVO;
import com.talented.buttie.user.dto.request.user.ModifyUserProfileRequest;
import com.talented.buttie.user.dto.request.user.UpdateEmploymentPreparationRequest;
import com.talented.buttie.user.dto.request.user.WithdrawUserRequest;
import com.talented.buttie.user.dto.response.user.GetButiDashboardResponse;
import com.talented.buttie.user.dto.response.user.GetEmploymentPreparationResponse;
import com.talented.buttie.user.dto.response.user.GetUserProfileResponse;
import com.talented.buttie.user.dto.response.user.UserPKResponse;
import com.talented.buttie.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "User")
@RestController
@RequestMapping("/api/users/my")
@RequiredArgsConstructor

public class UserController {

    private final UserService userService;

    @PatchMapping("/employment-preparation")
    @ApiOperation("취업 준비 정보 수정")
    public ApplicationResponse<UserPKResponse> modifyEmploymentPreparation(
        @AuthUser AuthenticationUser user,
        @Valid @RequestBody UpdateEmploymentPreparationRequest request
    ) {
        Long targetUserId = user.userId();
        Long userId = userService.modifyEmploymentPreparation(targetUserId, request);
        return ApplicationResponse.onSuccess(new UserPKResponse(PKCrypto.encrypt(userId)));
    }

//    @PostMapping("/employment-preparation")
//    @ApiOperation("취업 준비 정보 등록")
//    public ApplicationResponse<UserPKResponse> createEmploymentPreparation(
//        @AuthUser AuthenticationUser user,
//        @Valid @RequestBody CreateEmploymentPreparationRequest request
//    ) {
//        Long targetUserId = user.userId();
//        Long userId = userService.createEmploymentPreparation(targetUserId, request);
//        return ApplicationResponse.onSuccess(new UserPKResponse(PKCrypto.encrypt(userId)));
//    }

    @GetMapping("/info")
    @ApiOperation("마이페이지 내 정보 조회")
    public ApplicationResponse<GetUserProfileResponse> getUserProfile(
        @AuthUser AuthenticationUser user
    ) {
        Long targetUserId = user.userId();
        UserProfileVO userProfile = userService.getUserProfile(targetUserId);
        return ApplicationResponse.onSuccess(GetUserProfileResponse.from(userProfile));
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
    public ApplicationResponse<GetEmploymentPreparationResponse> getEmploymentPreparation(
        @AuthUser AuthenticationUser user
    ) {
        Long targetUserId = user.userId();
        EmploymentPreparationVO employmentPreparation = userService.getEmploymentPreparation(targetUserId);
        return ApplicationResponse.onSuccess(GetEmploymentPreparationResponse.from(employmentPreparation));
    }

    @PatchMapping
    @ApiOperation("회원 프로필 수정")
    public ApplicationResponse<UserPKResponse> modifyUserProfile(
        @AuthUser AuthenticationUser user,
        @Valid @RequestBody ModifyUserProfileRequest request
    ) {
        Long targetUserId = user.userId();
        Long modifiedUserId = userService.modifyUserProfile(targetUserId, request);
        return ApplicationResponse.onSuccess(new UserPKResponse(PKCrypto.encrypt(modifiedUserId)));
    }

    @DeleteMapping
    @ApiOperation("회원 탈퇴")
    public ApplicationResponse<UserPKResponse> withdrawUser(
        @AuthUser AuthenticationUser user,
        @Valid @RequestBody WithdrawUserRequest request
    ) {
        Long targetUserId = user.userId();
        Long withdrawnUserId = userService.withdrawUser(targetUserId, request);
        return ApplicationResponse.onSuccess(new UserPKResponse(PKCrypto.encrypt(withdrawnUserId)));
    }
}