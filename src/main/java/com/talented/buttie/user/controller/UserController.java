package com.talented.buttie.user.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.user.domain.UserProfileVO;
import com.talented.buttie.user.dto.response.GetUserProfileResponseDTO;
import com.talented.buttie.user.service.UserService;
import com.talented.buttie.user.domain.EmploymentPreparationVO;
import com.talented.buttie.user.dto.request.UpdateEmploymentPreparationRequestDTO;
import com.talented.buttie.user.dto.response.UserPKResponseDTO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ApplicationResponse<UserPKResponseDTO> saveEmploymentPreparation(
        @RequestParam Long userId,
        @Valid @RequestBody UpdateEmploymentPreparationRequestDTO request
    ) {
        EmploymentPreparationVO employmentPreparation = userService.saveEmploymentPreparation(userId, request);
        return ApplicationResponse.onSuccess(new UserPKResponseDTO(employmentPreparation.getUserId()));
    }

    @GetMapping
    @ApiOperation("회원 프로필 조회")
    public ApplicationResponse<GetUserProfileResponseDTO> getUserProfile(
        @ApiParam(value = "사용자 ID", required = true)
        @RequestParam Long userId
    ) {
        UserProfileVO vo = userService.getUserProfile(userId);
        return ApplicationResponse.onSuccess(GetUserProfileResponseDTO.from(vo));
    }
}