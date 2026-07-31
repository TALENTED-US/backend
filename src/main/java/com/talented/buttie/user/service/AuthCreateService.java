package com.talented.buttie.user.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.user.dto.request.auth.AuthSignUpRequestDTO;
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
    public Long createUser(AuthSignUpRequestDTO authSignUpRequestDTO) {
        isPasswordValid(authSignUpRequestDTO);
        isFieldDuplicate(authSignUpRequestDTO);

        String passwordHash = passwordEncoder.encode(authSignUpRequestDTO.userPassword());
        AuthSignUpRequestDTO newUser = authSignUpRequestDTO.saveNewUser(authSignUpRequestDTO, passwordHash);
        Long userId = authMapper.createUser(newUser);
        if(userId == null) {
            throw ApplicationException.from(AuthErrorCode.USER_CREATE_FAILED);
        }
        return userId;
    }

    private void isPasswordValid(AuthSignUpRequestDTO authSignUpRequestDTO) {
        String password = authSignUpRequestDTO.userPassword();
        if (!password.equals(authSignUpRequestDTO.userPasswordCheck())) {
            throw ApplicationException.from(AuthErrorCode.PASSWORD_NOT_VALID);
        }

        String username = authSignUpRequestDTO.userName().split("@")[0];
        if (password.contains(username)) {
            throw ApplicationException.from(AuthErrorCode.PASSWORD_HAS_USERNAME);
        }

        String middleNumber = authSignUpRequestDTO.phoneNumber().substring(3, 7);
        String lastNumber = authSignUpRequestDTO.phoneNumber().substring(7, 11);

        if (password.contains(middleNumber) || password.contains(lastNumber)) {
            throw ApplicationException.from(AuthErrorCode.PASSWORD_HAS_PHONENUMBER);
        }

        String[] birthday = authSignUpRequestDTO.birthDate().split("-");
        if (password.contains(birthday[1] + birthday[2])) {
            throw ApplicationException.from(AuthErrorCode.PASSWORD_HAS_BIRTHDAY);
        }
    }

    private void isFieldDuplicate(AuthSignUpRequestDTO authSignUpRequestDTO) {
        if (authMapper.existsByPhoneNumber(authSignUpRequestDTO.phoneNumber())) {
            throw ApplicationException.from(AuthErrorCode.PHONE_NUMBER_ALREADY_EXISTS);
        }

        if(authMapper.existsByEmail(authSignUpRequestDTO.userEmail())) {
            throw ApplicationException.from(AuthErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if(authMapper.existsByNickName(authSignUpRequestDTO.userNickname())) {
            throw ApplicationException.from(AuthErrorCode.NICKNAME_ALREADY_EXISTS);
        }
    }
}
