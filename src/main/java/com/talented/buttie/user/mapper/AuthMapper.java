package com.talented.buttie.user.mapper;

import com.talented.buttie.user.domain.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthMapper {

    Long createUser(@Param("userVO") UserVO userVO);

    boolean existsByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    boolean existsByEmail(@Param("email") String email);

    boolean existsByNickName(@Param("userNickname") String userNickname);

    String getPasswordByUserEmail(@Param("email") String email);

    Long getUserIdByUserEmail(@Param("email") String email);

    String getUserEmailByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    UserVO getUserByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    Long updateUserPassword(@Param("userId") Long userId, @Param("newPassword") String newPassword
    );

}
