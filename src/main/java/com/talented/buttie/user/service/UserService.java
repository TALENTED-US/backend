package com.talented.buttie.user.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.user.domain.UserProfileVO;
import com.talented.buttie.user.domain.UserVO;
import com.talented.buttie.user.dto.request.ModifyUserProfileRequestDTO;
import com.talented.buttie.user.exception.UserErrorCode;
import com.talented.buttie.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public UserProfileVO getUserProfile(Long userId) {
        UserProfileVO userProfile = userMapper.selectUserProfile(userId);

        if (userProfile == null) {
            throw ApplicationException.from(UserErrorCode.USER_NOT_FOUND);
        }

        return userProfile;
    }

    public UserVO modifyUserProfile(Long userId, ModifyUserProfileRequestDTO request) {
        if (userMapper.countByNickname(request.nickname()) > 0) {
            throw ApplicationException.from(UserErrorCode.DUPLICATE_NICKNAME);
        }

        UserVO user = UserVO.createModifiedUser(userId, request.nickname());
        int updatedRows = userMapper.updateUser(user);

        if (updatedRows == 0) {
            throw ApplicationException.from(UserErrorCode.USER_NOT_FOUND);
        }

        return user;
    }
}