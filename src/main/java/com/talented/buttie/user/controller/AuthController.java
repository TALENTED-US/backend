package com.talented.buttie.user.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.common.security.SecurityConstants;
import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.user.dto.request.auth.AuthLoginRequestDTO;
import com.talented.buttie.user.dto.request.auth.AuthSignUpRequestDTO;
import com.talented.buttie.user.dto.response.auth.TokenResponseDTO;
import com.talented.buttie.user.dto.response.UserPKResponseDTO;
import com.talented.buttie.user.service.AuthCreateService;
import com.talented.buttie.user.service.AuthReadService;
import io.swagger.annotations.Api;
import javax.validation.Valid;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
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
    private final AuthReadService authReadService;

    @PostMapping("/signUp")
    public ApplicationResponse<UserPKResponseDTO> signUp(@Valid @RequestBody AuthSignUpRequestDTO authSignUpRequestDTO){
        Long userId = authCreateService.createUser(authSignUpRequestDTO);
        return ApplicationResponse.onSuccess(new UserPKResponseDTO(PKCrypto.encrypt(userId)));
    }

    @PostMapping("/login")
    public ApplicationResponse<TokenResponseDTO> login(
        @Valid @RequestBody AuthLoginRequestDTO authLoginRequestDTO,
        HttpServletResponse response
    ) {
        TokenResponseDTO tokenResponseDTO = authReadService.userLogin(authLoginRequestDTO);

        Cookie refreshTokenCookie = new Cookie(
            SecurityConstants.REFRESH_TOKEN_COOKIE_NAME,
            tokenResponseDTO.refreshToken()
        );
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(Math.toIntExact(tokenResponseDTO.refreshTokenExpiration() / 1000));
        response.addCookie(refreshTokenCookie);

        return ApplicationResponse.onSuccess(tokenResponseDTO);
    }

}
