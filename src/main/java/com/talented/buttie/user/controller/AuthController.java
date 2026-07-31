package com.talented.buttie.user.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.user.dto.request.auth.AuthSignUpRequestDTO;
import com.talented.buttie.user.dto.response.UserPKResponseDTO;
import com.talented.buttie.user.service.AuthCreateService;
import io.swagger.annotations.Api;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Auth")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthCreateService authCreateService;

    @PostMapping("/signUp")
    public ApplicationResponse<UserPKResponseDTO> signUp(@Valid @RequestBody AuthSignUpRequestDTO authSignUpRequestDTO){
        Long userId = authCreateService.createUser(authSignUpRequestDTO);
        return ApplicationResponse.onSuccess(new UserPKResponseDTO(PKCrypto.encrypt(userId)));
    }


}
