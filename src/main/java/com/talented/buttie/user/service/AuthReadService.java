package com.talented.buttie.user.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.user.dto.request.auth.AuthLoginRequestDTO;
import com.talented.buttie.user.dto.response.auth.TokenResponseDTO;
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

    public TokenResponseDTO userLogin(@Valid AuthLoginRequestDTO authLoginRequestDTO) {
        if (!authMapper.existsByEmail(authLoginRequestDTO.userEmail())) {
            throw ApplicationException.from(AuthErrorCode.EMAIL_NOT_FOUND);
        }

        String targetPassword = authMapper.getPasswordByUserEmail(authLoginRequestDTO.userEmail());
        if (targetPassword == null) {
            throw ApplicationException.from(AuthErrorCode.EMAIL_NOT_FOUND);
        }

        if (!passwordEncoder.matches(authLoginRequestDTO.password(), targetPassword)) {
            throw ApplicationException.from(AuthErrorCode.PASSWORD_NOT_MATCH);
        }

        Long userId = authMapper.getUserIdByUserEmail(authLoginRequestDTO.userEmail());
        if (userId == null) {
            throw ApplicationException.from(AuthErrorCode.EMAIL_NOT_FOUND);
        }

        return authTokenService.createToken(userId);
    }
}
