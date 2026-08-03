package com.talented.buttie.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.talented.buttie.user.domain.UserProfileVO;
import com.talented.buttie.user.domain.UserVO;


@Mapper
public interface UserMapper {

    UserProfileVO selectUserProfile(@Param("userId") Long userId);

    int updateUser(@Param("user") UserVO user);

    int countByNickname(@Param("nickname") String nickname);

}
