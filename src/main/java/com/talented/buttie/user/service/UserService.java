package com.talented.buttie.user.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.user.domain.EmploymentPreparationVO;
import com.talented.buttie.user.domain.UserProfileVO;
import com.talented.buttie.user.domain.UserVO;
import com.talented.buttie.user.dto.request.CreateEmploymentPreparationRequestDTO;
import com.talented.buttie.user.dto.request.ModifyUserProfileRequestDTO;
import com.talented.buttie.user.dto.request.UpdateEmploymentPreparationRequestDTO;
import com.talented.buttie.user.dto.request.WithdrawUserRequestDTO;
import com.talented.buttie.user.exception.UserErrorCode;
import com.talented.buttie.user.mapper.EmploymentPreparationMapper;
import com.talented.buttie.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmploymentPreparationMapper employmentPreparationMapper;

    public UserProfileVO getUserProfile(Long userId) {
        UserProfileVO userProfile = userMapper.selectUserProfile(userId);

        if (userProfile == null) {
            throw ApplicationException.from(UserErrorCode.USER_NOT_FOUND);
        }

        return userProfile;
    }

    public Long modifyEmploymentPreparation(Long userId, UpdateEmploymentPreparationRequestDTO request) {
        EmploymentPreparationVO employmentPreparation =
            EmploymentPreparationVO.createEmploymentPreparation(userId, request);

        int updated = employmentPreparationMapper.updateEmploymentPreparation(employmentPreparation);
        if (updated == 0) {
            throw ApplicationException.from(UserErrorCode.EMPLOYMENT_PREPARATION_NOT_FOUND);
        }
        return userId;
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

    public Long createEmploymentPreparation(Long userId, CreateEmploymentPreparationRequestDTO request) {

        EmploymentPreparationVO employmentPreparation =
            EmploymentPreparationVO.createEmploymentPreparation(userId, request);

        int inserted = employmentPreparationMapper.insertEmploymentPreparation(employmentPreparation);
        if (inserted == 0) {
            throw ApplicationException.from(UserErrorCode.EMPLOYMENT_PREPARATION_CREATE_FAILED);
        }
        return userId;
    }

    public Long modifyUserProfile(Long userId, ModifyUserProfileRequestDTO request) {
        if (userMapper.countByNickname(request.nickname()) > 0) {
            throw ApplicationException.from(UserErrorCode.DUPLICATE_NICKNAME);
        }

        UserVO user = UserVO.createModifiedUser(userId, request.nickname());
        int updatedUserId = userMapper.updateUser(user);

        if (updatedUserId == 0) {
            throw ApplicationException.from(UserErrorCode.USER_NOT_FOUND);
        }

        return userId;
    }
}
