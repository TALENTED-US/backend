package com.talented.buttie.user.mapper;

import com.talented.buttie.user.domain.ButtieDashboardVO;
import com.talented.buttie.user.domain.UserProfileVO;
import com.talented.buttie.user.domain.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    UserProfileVO selectUserProfile(@Param("userId") Long userId);

    ButtieDashboardVO selectButtieDashboard(@Param("userId") Long userId);

    int updateUser(@Param("user") UserVO user);

    int countByNickname(@Param("nickname") String nickname);

    String getPasswordByUserId(@Param("userId") Long userId);

    int updateWithdrawnUser(@Param("user") UserVO user);

    Long createUserButtie(@Param("userId") Long userId);

    Long createUserConsent(Long userId);

    int deductUserExp(@Param("userId") Long userId, @Param("expAmount") Integer expAmount);

    int addUserExp(@Param("userId") Long userId, @Param("expAmount") Integer expAmount);
}
