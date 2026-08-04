package com.talented.buttie.user.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.common.security.AuthenticationUser;
import com.talented.buttie.common.security.SecurityConstants;
import com.talented.buttie.common.security.annotation.AuthUser;
import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.user.dto.request.auth.AuthLoginRequest;
import com.talented.buttie.user.dto.request.auth.AuthSignUpRequest;
import com.talented.buttie.user.dto.response.UserPKResponseDTO;
import com.talented.buttie.user.dto.response.auth.AuthTokenResponseDTO;
import com.talented.buttie.user.service.AuthCookieService;
import com.talented.buttie.user.dto.response.auth.AuthDuplicateCheckResponseDTO;
import com.talented.buttie.user.service.AuthCreateService;
import com.talented.buttie.user.service.AuthReadService;
import com.talented.buttie.user.service.AuthTokenService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Auth")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthCreateService authCreateService;
    private final AuthReadService authReadService;
    private final AuthTokenService authTokenService;
    private final AuthCookieService authCookieService;

    @ApiOperation("사용자 회원 가입")
    @PostMapping("/signUp")
    public ApplicationResponse<UserPKResponseDTO> signUp(@Valid @RequestBody AuthSignUpRequest authSignUpRequest) {
        Long userId = authCreateService.createUser(authSignUpRequest);
        return ApplicationResponse.onSuccess(new UserPKResponseDTO(PKCrypto.encrypt(userId)));
    }

    @ApiOperation("사용자 로그인")
    @PostMapping("/login")
    public ApplicationResponse<AuthTokenResponseDTO> login(@Valid @RequestBody AuthLoginRequest authLoginRequest, HttpServletResponse response) {
        AuthTokenResponseDTO tokenResponseDTO = authReadService.userLogin(authLoginRequest);

        int maxAge = Math.toIntExact(tokenResponseDTO.refreshTokenExpiration() / 1000);
        response.addCookie(authCookieService.createCookie(SecurityConstants.REFRESH_TOKEN_COOKIE_NAME, tokenResponseDTO.refreshToken(), true, maxAge));
        response.addCookie(authCookieService.createCookie(SecurityConstants.CSRF_TOKEN_COOKIE_NAME, UUID.randomUUID().toString(), false, maxAge));

        return ApplicationResponse.onSuccess(tokenResponseDTO);
    }

    @ApiOperation("사용자 로그아웃")
    @DeleteMapping("/logout")
    public ApplicationResponse<UserPKResponseDTO> logout(@AuthUser AuthenticationUser authenticationUser, HttpServletResponse response) {
        Long targetUserId = authenticationUser.userId();
        Long userId = authTokenService.expirationToken(targetUserId);
        response.addCookie(authCookieService.createCookie(SecurityConstants.REFRESH_TOKEN_COOKIE_NAME, "", true, 0));
        response.addCookie(authCookieService.createCookie(SecurityConstants.CSRF_TOKEN_COOKIE_NAME, "", false, 0));

        return ApplicationResponse.onSuccess(new UserPKResponseDTO(PKCrypto.encrypt(userId)));
    }

    @ApiOperation("사용자 이메일 중복확인")
    @GetMapping("/check-email")
    public ApplicationResponse<AuthDuplicateCheckResponseDTO> checkEmailDuplicate(@RequestParam String email) {
        boolean isDuplicate = authReadService.isEmailDuplicate(email);
        return ApplicationResponse.onSuccess(new AuthDuplicateCheckResponseDTO(isDuplicate));
    }

    @ApiOperation("사용자 닉네임 중복확인")
    @GetMapping("/check-nickname")
    public ApplicationResponse<AuthDuplicateCheckResponseDTO> checkNicknameDuplicate(@RequestParam String nickname) {
        boolean isDuplicate = authReadService.isNicknameDuplicate(nickname);
        return ApplicationResponse.onSuccess(new AuthDuplicateCheckResponseDTO(isDuplicate));
    }
}
