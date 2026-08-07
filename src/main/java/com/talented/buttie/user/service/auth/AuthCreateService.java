package com.talented.buttie.user.service.auth;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.user.dto.request.auth.AuthSignUpRequest;
import com.talented.buttie.user.exception.AuthErrorCode;
import com.talented.buttie.user.mapper.AuthMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthCreateService {

    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long createUser(AuthSignUpRequest authSignUpRequest) {
        isPasswordValid(authSignUpRequest);
        isFieldDuplicate(authSignUpRequest);

        String passwordHash = passwordEncoder.encode(authSignUpRequest.userPassword());
        AuthSignUpRequest newUser = authSignUpRequest.saveNewUser(authSignUpRequest, passwordHash);
        Long userId = authMapper.createUser(newUser);
        if(userId == null) {
            throw ApplicationException.from(AuthErrorCode.USER_CREATE_FAILED);
        }
        return userId;
    }

    private void isPasswordValid(AuthSignUpRequest authSignUpRequest) {
        String password = authSignUpRequest.userPassword();
        if (!password.equals(authSignUpRequest.userPasswordCheck())) {
            throw ApplicationException.from(AuthErrorCode.PASSWORD_NOT_VALID);
        }

        String username = authSignUpRequest.userName().split("@")[0];
        if (password.contains(username)) {
            throw ApplicationException.from(AuthErrorCode.PASSWORD_HAS_USERNAME);
        }

        String middleNumber = authSignUpRequest.phoneNumber().substring(3, 7);
        String lastNumber = authSignUpRequest.phoneNumber().substring(7, 11);

        if (password.contains(middleNumber) || password.contains(lastNumber)) {
            throw ApplicationException.from(AuthErrorCode.PASSWORD_HAS_PHONENUMBER);
        }

        String[] birthday = authSignUpRequest.birthDate().split("-");
        if (password.contains(birthday[1] + birthday[2])) {
            throw ApplicationException.from(AuthErrorCode.PASSWORD_HAS_BIRTHDAY);
        }
    }

    private void isFieldDuplicate(AuthSignUpRequest authSignUpRequest) {
        if (authMapper.existsByPhoneNumber(authSignUpRequest.phoneNumber())) {
            throw ApplicationException.from(AuthErrorCode.PHONE_NUMBER_ALREADY_EXISTS);
        }

        if(authMapper.existsByEmail(authSignUpRequest.userEmail())) {
            throw ApplicationException.from(AuthErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if(authMapper.existsByNickName(authSignUpRequest.userNickname())) {
            throw ApplicationException.from(AuthErrorCode.NICKNAME_ALREADY_EXISTS);
        }
    }
}
