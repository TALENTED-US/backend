package com.talented.buttie.user.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.user.dto.request.UpdateEmploymentPreparationRequestDTO;
import com.talented.buttie.user.dto.response.UserPKResponseDTO;
import com.talented.buttie.user.service.UserService;

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
        userService.saveEmploymentPreparation(userId, request);
        return ApplicationResponse.onSuccess(new UserPKResponseDTO(userId));
    }
}