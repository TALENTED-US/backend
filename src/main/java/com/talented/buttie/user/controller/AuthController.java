package com.talented.buttie.user.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.common.security.AuthenticationUser;
import com.talented.buttie.common.security.SecurityConstants;
import com.talented.buttie.common.security.annotation.AuthUser;
import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.user.dto.request.auth.AuthLoginRequest;
import com.talented.buttie.user.dto.request.auth.AuthPasswordRequest;
import com.talented.buttie.user.dto.request.auth.AuthSignUpRequest;
import com.talented.buttie.user.dto.request.auth.AuthVerifyRequest;
import com.talented.buttie.user.dto.response.auth.AuthDuplicateCheckResponse;
import com.talented.buttie.user.dto.response.auth.AuthEmailResponse;
import com.talented.buttie.user.dto.response.auth.AuthTokenResponse;
import com.talented.buttie.user.dto.response.auth.AuthVerifyResponse;
import com.talented.buttie.user.dto.response.user.UserPKResponse;
import com.talented.buttie.user.service.auth.AuthCookieService;
import com.talented.buttie.user.service.auth.AuthCreateService;
import com.talented.buttie.user.service.auth.AuthReadService;
import com.talented.buttie.user.service.auth.AuthTokenService;
import com.talented.buttie.user.service.auth.AuthUpdateService;
import com.talented.buttie.user.service.auth.PortOneService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@Api(tags = "Auth")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String IDENTITY_VERIFICATION_TOKEN_HEADER = "X-Identity-Verification-Token";

    private final AuthCreateService authCreateService;
    private final AuthReadService authReadService;
    private final AuthTokenService authTokenService;
    private final AuthCookieService authCookieService;
    private final PortOneService portOneService;
    private final AuthUpdateService authUpdateService;

    @ApiOperation("사용자 회원 가입")
    @PostMapping("/signUp")
    public ApplicationResponse<UserPKResponse> signUp(
        @RequestHeader(IDENTITY_VERIFICATION_TOKEN_HEADER) String verificationToken,
        @Valid @RequestBody AuthSignUpRequest authSignUpRequest
    ) {
        Long userId = authCreateService.createUser(authSignUpRequest, verificationToken);
        return ApplicationResponse.onSuccess(new UserPKResponse(PKCrypto.encrypt(userId)));
    }

    @ApiOperation("사용자 동의")
    @PostMapping("/consent/{userId}")
    public ApplicationResponse<UserPKResponse> createUserConsent(
        @PathVariable String userId
    ) {
        Long targetUserId = authCreateService.createUserConsent(PKCrypto.decrypt(userId));
        return ApplicationResponse.onSuccess(new UserPKResponse(PKCrypto.encrypt(targetUserId)));
    }

    @ApiOperation("사용자 로그인")
    @PostMapping("/login")
    public ApplicationResponse<AuthTokenResponse> login(
        @Valid @RequestBody AuthLoginRequest authLoginRequest,
        HttpServletResponse response
    ) {
        AuthTokenResponse tokenResponse = authReadService.userLogin(authLoginRequest);

        int maxAge = Math.toIntExact(tokenResponse.refreshTokenExpiration() / 1000);
        addAuthCookies(response, tokenResponse.refreshToken(), maxAge);

        return ApplicationResponse.onSuccess(tokenResponse);
    }

    @ApiOperation("사용자 로그아웃")
    @DeleteMapping("/logout")
    public ApplicationResponse<UserPKResponse> logout(
        @AuthUser AuthenticationUser authenticationUser,
        HttpServletResponse response
    ) {
        Long targetUserId = authenticationUser.userId();
        Long userId = authTokenService.expirationToken(targetUserId);
        addAuthCookies(response, "", 0);

        return ApplicationResponse.onSuccess(new UserPKResponse(PKCrypto.encrypt(userId)));
    }

    @ApiOperation("사용자 이메일 중복확인")
    @GetMapping("/check-email")
    public ApplicationResponse<AuthDuplicateCheckResponse> checkEmailDuplicate(@RequestParam String email) {
        boolean isDuplicate = authReadService.isEmailDuplicate(email);
        return ApplicationResponse.onSuccess(new AuthDuplicateCheckResponse(isDuplicate));
    }

    @ApiOperation("사용자 닉네임 중복확인")
    @GetMapping("/check-nickname")
    public ApplicationResponse<AuthDuplicateCheckResponse> checkNicknameDuplicate(@RequestParam String nickname) {
        boolean isDuplicate = authReadService.isNicknameDuplicate(nickname);
        return ApplicationResponse.onSuccess(new AuthDuplicateCheckResponse(isDuplicate));
    }

    @ApiOperation("액세스 토큰 재발급")
    @GetMapping("/reissue")
    public ApplicationResponse<AuthTokenResponse> reissue(
        @ApiIgnore @CookieValue(SecurityConstants.REFRESH_TOKEN_COOKIE_NAME) String refreshToken,
        HttpServletResponse response
    ) {
        AuthTokenResponse tokenResponse = authTokenService.reissue(refreshToken);

        int maxAge = Math.toIntExact(tokenResponse.refreshTokenExpiration() / 1000);
        addAuthCookies(response, tokenResponse.refreshToken(), maxAge);

        return ApplicationResponse.onSuccess(tokenResponse);
    }

    @ApiOperation("사용자 본인 인증 PortOne")
    @PostMapping("/verify")
    public ApplicationResponse<AuthVerifyResponse> verifyUser(
        @Valid @RequestBody AuthVerifyRequest authVerifyRequest,
        HttpServletResponse response
    ) {
        AuthVerifyResponse authVerifyResponse = portOneService.verifyUser(authVerifyRequest);
        response.setHeader(IDENTITY_VERIFICATION_TOKEN_HEADER, authVerifyResponse.token());
        return ApplicationResponse.onSuccess(authVerifyResponse);
    }

    @ApiOperation("사용자 userEmail 조회")
    @GetMapping("/email")
    public ApplicationResponse<AuthEmailResponse> getUserEmail(
        @RequestHeader(IDENTITY_VERIFICATION_TOKEN_HEADER) String verificationToken
    ) {
        String userEmail = authReadService.getUserEmail(verificationToken);
        return ApplicationResponse.onSuccess(new AuthEmailResponse(userEmail));
    }

    @ApiOperation("사용자 비밀번호 수정")
    @PatchMapping("/password")
    public ApplicationResponse<UserPKResponse> updateUserPassword(
        @RequestHeader(IDENTITY_VERIFICATION_TOKEN_HEADER) String verificationToken,
        @Valid @RequestBody AuthPasswordRequest authPasswordRequest
    ) {
        Long updatedUserId = authUpdateService.updateUserPassword(verificationToken, authPasswordRequest);
        return ApplicationResponse.onSuccess(new UserPKResponse(PKCrypto.encrypt(updatedUserId)));
    }

    private void addAuthCookies(
        HttpServletResponse response,
        String refreshToken,
        int maxAge
    ) {
        response.addHeader(
            "Set-Cookie",
            authCookieService.createCookie(
                SecurityConstants.REFRESH_TOKEN_COOKIE_NAME,
                refreshToken,
                true,
                maxAge
            )
        );
        response.addHeader(
            "Set-Cookie",
            authCookieService.createCookie(
                SecurityConstants.CSRF_TOKEN_COOKIE_NAME,
                maxAge == 0 ? "" : UUID.randomUUID().toString(),
                false,
                maxAge
            )
        );
    }


}
