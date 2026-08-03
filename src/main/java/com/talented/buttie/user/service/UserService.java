package com.talented.buttie.user.service;

import com.talented.buttie.user.domain.EmploymentPreparationVO;
import com.talented.buttie.user.dto.request.UpdateEmploymentPreparationRequestDTO;
import com.talented.buttie.user.mapper.EmploymentPreparationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.user.domain.UserProfileVO;
import com.talented.buttie.user.exception.UserErrorCode;
import com.talented.buttie.user.dto.request.CreateEmploymentPreparationRequestDTO;
import com.talented.buttie.user.mapper.UserMapper;

@Service
@RequiredArgsConstructor
public class UserService {
    private final EmploymentPreparationMapper employmentPreparationMapper;
    private final UserMapper userMapper;

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

    public Long createEmploymentPreparation(Long userId, CreateEmploymentPreparationRequestDTO request) {

        EmploymentPreparationVO employmentPreparation =
            EmploymentPreparationVO.createEmploymentPreparation(userId, request);

        int inserted = employmentPreparationMapper.insertEmploymentPreparation(employmentPreparation);
        if (inserted == 0) {
            throw ApplicationException.from(UserErrorCode.EMPLOYMENT_PREPARATION_CREATE_FAILED);
        }
        return userId;
    }


    public UserProfileVO getUserProfile(Long userId) {
        UserProfileVO vo = userMapper.selectUserProfile(userId);

        if (vo == null) {
            throw ApplicationException.from(UserErrorCode.USER_NOT_FOUND);
        }

        return vo;
    }
}
