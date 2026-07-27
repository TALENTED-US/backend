package com.talented.buttie.user.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.user.domain.UserProfileVO;
import com.talented.buttie.user.exception.UserErrorCode;
import com.talented.buttie.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public UserProfileVO getUserProfile(Long userId) {
        UserProfileVO vo = userMapper.selectUserProfile(userId);

        if (vo == null) {
            throw ApplicationException.from(UserErrorCode.USER_NOT_FOUND);
        }

        return vo;
    }
}