package com.talented.buttie.user.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.user.domain.EmploymentPreparationVO;
import com.talented.buttie.user.domain.UserProfileVO;
import com.talented.buttie.user.domain.UserVO;
import com.talented.buttie.user.dto.request.ModifyUserProfileRequestDTO;
import com.talented.buttie.user.dto.request.UpdateEmploymentPreparationRequestDTO;
import com.talented.buttie.user.exception.UserErrorCode;
import com.talented.buttie.user.mapper.EmploymentPreparationMapper;
import com.talented.buttie.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final EmploymentPreparationMapper employmentPreparationMapper;

    public UserProfileVO getUserProfile(Long userId) {
        UserProfileVO userProfile = userMapper.selectUserProfile(userId);

        if (userProfile == null) {
            throw ApplicationException.from(UserErrorCode.USER_NOT_FOUND);
        }

        return userProfile;
    }

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

    public Long modifyUserProfile(Long userId, ModifyUserProfileRequestDTO request) {
        if (userMapper.countByNickname(request.nickname()) > 0) {
            throw ApplicationException.from(UserErrorCode.DUPLICATE_NICKNAME);
        }

        UserVO user = UserVO.createModifiedUser(userId, request.nickname());
        int updatedRows = userMapper.updateUser(user);

        if (updatedRows == 0) {
            throw ApplicationException.from(UserErrorCode.USER_NOT_FOUND);
        }

        return userId;
    }
}