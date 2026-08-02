package com.talented.buttie.user.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.user.domain.EmploymentPreparationVO;
import com.talented.buttie.user.dto.request.UpdateEmploymentPreparationRequestDTO;
import com.talented.buttie.user.dto.response.UserPKResponseDTO;
import com.talented.buttie.user.service.UserService;
import com.talented.buttie.user.dto.request.CreateEmploymentPreparationRequestDTO;
import com.talented.buttie.common.security.annotation.AuthUser;
import com.talented.buttie.common.security.AuthenticationUser;
import org.springframework.web.bind.annotation.PostMapping;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
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
        return ApplicationResponse.onSuccess(new UserPKResponseDTO(PKCrypto.encrypt(employmentPreparation.getUserId())));
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
}