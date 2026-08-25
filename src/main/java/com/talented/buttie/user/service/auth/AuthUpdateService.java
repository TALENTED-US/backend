package com.talented.buttie.user.service.auth;

import com.github.benmanes.caffeine.cache.Cache;
import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.user.domain.UserVO;
import com.talented.buttie.user.dto.request.auth.AuthPasswordRequest;
import com.talented.buttie.user.dto.response.auth.VerifiedCustomer;
import com.talented.buttie.user.exception.AuthErrorCode;
import com.talented.buttie.user.mapper.AuthMapper;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthUpdateService {

    private final AuthMapper authMapper;
    private final Cache<String, VerifiedCustomer> identityVerificationCache;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;

    @Transactional
    public Long updateUserPassword(String verificationToken, @Valid AuthPasswordRequest authPasswordRequest) {
        String newPassword = authPasswordRequest.password();
        if (!newPassword.equals(authPasswordRequest.passwordCheck())) {
            throw ApplicationException.from(AuthErrorCode.PASSWORD_NOT_MATCH);
        }

        VerifiedCustomer verifiedCustomer = identityVerificationCache.getIfPresent(verificationToken);
        if (verifiedCustomer == null) {
            throw ApplicationException.from(AuthErrorCode.INVALID_TOKEN);
        }

        UserVO userVO = authMapper.getUserByPhoneNumber(verifiedCustomer.phoneNumber());
        if (userVO == null) {
            throw ApplicationException.from(AuthErrorCode.USER_NOT_FOUND);
        }

        isPasswordValid(userVO, newPassword, verifiedCustomer);

        String encodedNewPassword = passwordEncoder.encode(newPassword);
        Long updatedRows = authMapper.updateUserPassword(userVO.getUserId(), encodedNewPassword);
        if (updatedRows == 0) {
            throw ApplicationException.from(AuthErrorCode.PASSWORD_UPDATE_FAILED);
        }

        authTokenService.expirationToken(userVO.getUserId());
        identityVerificationCache.invalidate(verificationToken);
        return userVO.getUserId();
    }


    private void isPasswordValid(UserVO userVO, String password, VerifiedCustomer verifiedCustomer) {
        String userEmail = userVO.getUserEmail().split("@")[0];
        if (password.contains(userEmail)) {
            throw ApplicationException.from(AuthErrorCode.PASSWORD_HAS_EMAIL);
        }

        String phoneNumber = verifiedCustomer.phoneNumber().replaceAll("\\D", "");
        String middleNumber = phoneNumber.substring(3, 7);
        String lastNumber = phoneNumber.substring(7, 11);

        if (password.contains(middleNumber) || password.contains(lastNumber)) {
            throw ApplicationException.from(AuthErrorCode.PASSWORD_HAS_PHONENUMBER);
        }

        String[] birthday = verifiedCustomer.birthDate().split("-");
        if (password.contains(birthday[1] + birthday[2])) {
            throw ApplicationException.from(AuthErrorCode.PASSWORD_HAS_BIRTHDAY);
        }
    }
}
