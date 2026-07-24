package com.talented.buttie.user.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.user.dto.request.CreateEmploymentPreparationRequestDTO;
import com.talented.buttie.user.service.UserService;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "User")
@RestController
@RequestMapping("/api/users/my")
@RequiredArgsConstructor

public class UserController {

    private final UserService userService;

    @PostMapping("/employment-preparation")
    public ApplicationResponse<Void> createEmploymentPreparation(
        @RequestParam Long userId,
        @RequestBody CreateEmploymentPreparationRequestDTO request

    ) {
        userService.createEmploymentPreparation(userId, request);
        return ApplicationResponse.onSuccess();
  }
}