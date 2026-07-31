package com.talented.buttie.user.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.user.domain.EmploymentPreparationVO;
import com.talented.buttie.user.dto.request.UpdateEmploymentPreparationRequestDTO;
import com.talented.buttie.user.dto.response.UserPKResponseDTO;
import com.talented.buttie.user.service.UserService;
import com.talented.buttie.user.dto.response.GetEmploymentPreparationResponseDTO;
import com.talented.buttie.user.dto.request.WithdrawUserRequestDTO;
import com.talented.buttie.common.security.AuthenticationUser;
import com.talented.buttie.common.security.annotation.AuthUser;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import javax.validation.Valid;

@Api(tags = "User")
@RestController
@RequestMapping("/api/users/my")
@RequiredArgsConstructor

public class UserController {

    private final UserService userService;

    @PatchMapping("/employment-preparation")
    public ApplicationResponse<UserPKResponseDTO> saveEmploymentPreparation(
        @RequestParam Long userId,
        @Valid @RequestBody UpdateEmploymentPreparationRequestDTO request
    ) {
        EmploymentPreparationVO employmentPreparation = userService.saveEmploymentPreparation(userId, request);
        return ApplicationResponse.onSuccess(new UserPKResponseDTO(PKCrypto.encrypt(employmentPreparation.getUserId())));
    }

    @GetMapping("/employment-preparation")
    public ApplicationResponse<GetEmploymentPreparationResponseDTO> getEmploymentPreparation(
        @RequestParam Long userId
    ) {
        EmploymentPreparationVO employmentPreparation = userService.getEmploymentPreparation(userId);
        return ApplicationResponse.onSuccess(GetEmploymentPreparationResponseDTO.from(employmentPreparation));
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