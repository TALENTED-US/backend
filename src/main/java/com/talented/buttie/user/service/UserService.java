package com.talented.buttie.user.service;

import com.talented.buttie.user.domain.EmploymentPreparationVO;
import com.talented.buttie.user.dto.request.UpdateEmploymentPreparationRequestDTO;
import com.talented.buttie.user.mapper.EmploymentPreparationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.user.exception.UserErrorCode;
import com.talented.buttie.user.domain.UserVO;
import com.talented.buttie.user.dto.request.WithdrawUserRequestDTO;
import com.talented.buttie.user.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;



@Service
@RequiredArgsConstructor
public class UserService {
    private final EmploymentPreparationMapper employmentPreparationMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public EmploymentPreparationVO saveEmploymentPreparation(Long userId, UpdateEmploymentPreparationRequestDTO request) {

        EmploymentPreparationVO employmentPreparation =
            EmploymentPreparationVO.createEmploymentPreparation(userId, request);

        int updated = employmentPreparationMapper.updateEmploymentPreparation(employmentPreparation);
        if (updated == 0) {
            throw ApplicationException.from(UserErrorCode.EMPLOYMENT_PREPARATION_NOT_FOUND);
        }
        return employmentPreparation;
    }
    public EmploymentPreparationVO getEmploymentPreparation(Long userId) {
        EmploymentPreparationVO employmentPreparation =
            employmentPreparationMapper.selectEmploymentPreparation(userId);

        if (employmentPreparation == null) {
            throw ApplicationException.from(UserErrorCode.EMPLOYMENT_PREPARATION_NOT_FOUND);
        }

        return employmentPreparation;
    }

    public Long withdrawUser(Long userId, WithdrawUserRequestDTO request) {
        UserVO user = userMapper.selectUserById(userId);

        if (user == null) {
            throw ApplicationException.from(UserErrorCode.USER_NOT_FOUND);
        }

        if (!passwordEncoder.matches(request.password(), user.getUserPasswordHash())){
            throw ApplicationException.from(UserErrorCode.PASSWORD_MISMATCH);
        }

        UserVO withdrawnUser = UserVO.createWithdrawnUser(userId);
        userMapper.updateWithdrawnUser(withdrawnUser);

        return userId;
    }
}
