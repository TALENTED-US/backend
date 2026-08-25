package com.talented.buttie.user.service.auth;

import com.github.benmanes.caffeine.cache.Cache;
import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.user.domain.EmploymentPreparationType;
import com.talented.buttie.user.domain.EmploymentPreparationVO;
import com.talented.buttie.user.domain.UserVO;
import com.talented.buttie.user.dto.request.auth.AuthSignUpRequest;
import com.talented.buttie.user.dto.response.auth.VerifiedCustomer;
import com.talented.buttie.user.exception.AuthErrorCode;
import com.talented.buttie.user.mapper.AuthMapper;
import com.talented.buttie.user.mapper.EmploymentPreparationMapper;
import com.talented.buttie.user.mapper.UserMapper;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthCreateService {

    private final AuthMapper authMapper;
    private final UserMapper userMapper;
    private final EmploymentPreparationMapper employmentPreparationMapper;
    private final PasswordEncoder passwordEncoder;
    private final Cache<String, VerifiedCustomer> identityVerificationCache;

    @Transactional
    public Long createUser(AuthSignUpRequest authSignUpRequest, String verificationToken) {
        VerifiedCustomer verifiedCustomer = getVerifiedCustomer(verificationToken);

        isPasswordValid(authSignUpRequest, verifiedCustomer);
        isFieldDuplicate(authSignUpRequest, verifiedCustomer);

        String passwordHash = passwordEncoder.encode(authSignUpRequest.userPassword());

        UserVO userVO = UserVO.createUser(authSignUpRequest, verifiedCustomer, passwordHash);
        Long insertRows = authMapper.createUser(userVO);
        if (insertRows == null || insertRows == 0) {
            throw ApplicationException.from(AuthErrorCode.USER_CREATE_FAILED);
        }
        Long userId = userVO.getUserId();
        if (userId == null) {
            throw ApplicationException.from(AuthErrorCode.USER_CREATE_FAILED);
        }

        Long preparationUserId = employmentPreparationMapper.createEmploymentPreparation(
            EmploymentPreparationVO.builder()
                .userId(userId)
                .birthDate(LocalDate.parse(verifiedCustomer.birthDate()))
                .employmentPrepRegion("서울특별시")
                .familyCount(1)
                .employmentPrepType(EmploymentPreparationType.FIRST_JOB)
                .prepStartDate(LocalDate.now())
                .targetEmploymentDate(LocalDate.now().plusMonths(6))
                .livingFundThreshold(500000)
                .build());

        if (preparationUserId == null || preparationUserId == 0) {
            throw ApplicationException.from(AuthErrorCode.USER_CREATE_FAILED);
        }

        Long buttieUserId = userMapper.createUserButtie(userId);
        if (buttieUserId == null || buttieUserId == 0) {
            throw ApplicationException.from(AuthErrorCode.USER_CREATE_FAILED);
        }

        identityVerificationCache.invalidate(verificationToken);
        return userId;
    }

    public Long createUserConsent(Long userId) {
        Long targetUserId = userMapper.createUserConsent(userId);
        if (targetUserId == null) {
            throw ApplicationException.from(AuthErrorCode.USER_CREATE_FAILED);
        }
        return userId;
    }

    private VerifiedCustomer getVerifiedCustomer(String token) {
        VerifiedCustomer verifiedCustomer = identityVerificationCache.getIfPresent(token);
        if (verifiedCustomer == null) {
            throw ApplicationException.from(AuthErrorCode.IDENTITY_VERIFICATION_FAILED);
        }
        return verifiedCustomer;
    }

    private void isPasswordValid(AuthSignUpRequest authSignUpRequest, VerifiedCustomer verifiedCustomer) {
        String password = authSignUpRequest.userPassword();
        if (!password.equals(authSignUpRequest.userPasswordCheck())) {
            throw ApplicationException.from(AuthErrorCode.PASSWORD_NOT_VALID);
        }

        String userEmail = authSignUpRequest.userEmail().split("@")[0];
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

    private void isFieldDuplicate(AuthSignUpRequest authSignUpRequest, VerifiedCustomer verifiedCustomer) {
        if (authMapper.existsByPhoneNumber(verifiedCustomer.phoneNumber())) {
            throw ApplicationException.from(AuthErrorCode.PHONE_NUMBER_ALREADY_EXISTS);
        }

        if (authMapper.existsByEmail(authSignUpRequest.userEmail())) {
            throw ApplicationException.from(AuthErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (authMapper.existsByNickName(authSignUpRequest.userNickname())) {
            throw ApplicationException.from(AuthErrorCode.NICKNAME_ALREADY_EXISTS);
        }
    }


}
