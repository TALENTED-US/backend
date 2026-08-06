package com.talented.buttie.user.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.user.dto.request.auth.AuthLoginRequest;
import com.talented.buttie.user.dto.response.auth.AuthTokenResponseDTO;
import com.talented.buttie.user.exception.AuthErrorCode;
import com.talented.buttie.user.mapper.AuthMapper;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthReadService {

    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;

    public AuthTokenResponseDTO userLogin(@Valid AuthLoginRequest authLoginRequest) {
        if (!authMapper.existsByEmail(authLoginRequest.userEmail())) {
            throw ApplicationException.from(AuthErrorCode.EMAIL_NOT_FOUND);
        }

        String targetPassword = authMapper.getPasswordByUserEmail(authLoginRequest.userEmail());
        if (targetPassword == null) {
            throw ApplicationException.from(AuthErrorCode.EMAIL_NOT_FOUND);
        }

        if (!passwordEncoder.matches(authLoginRequest.password(), targetPassword)) {
            throw ApplicationException.from(AuthErrorCode.PASSWORD_NOT_MATCH);
        }

        Long userId = authMapper.getUserIdByUserEmail(authLoginRequest.userEmail());
        if (userId == null) {
            throw ApplicationException.from(AuthErrorCode.EMAIL_NOT_FOUND);
        }

        return authTokenService.createToken(userId);
    }

    public boolean isEmailDuplicate(String email) {
        boolean isDuplicate = authMapper.existsByEmail(email);
        if (isDuplicate) {
            throw ApplicationException.from(AuthErrorCode.EMAIL_ALREADY_EXISTS);
        }
        return isDuplicate;
    }

    public boolean isNicknameDuplicate(String nickname) {
        boolean isDuplicate = authMapper.existsByNickName(nickname);
        if (isDuplicate) {
            throw ApplicationException.from(AuthErrorCode.NICKNAME_ALREADY_EXISTS);
        }
        return isDuplicate;
    }
}
