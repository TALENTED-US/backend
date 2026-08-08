package com.talented.buttie.user.mapper;

import com.talented.buttie.user.dto.request.auth.AuthSignUpRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthMapper {
    Long createUser(@Param("authSignUpRequestDTO") AuthSignUpRequest authSignUpRequest);
    boolean existsByPhoneNumber(@Param("phoneNumber") String phoneNumber);
    boolean existsByEmail(@Param("email") String email);
    boolean existsByNickName(@Param("userNickname") String userNickname);
    String getPasswordByUserEmail(@Param("email") String email);
    Long getUserIdByUserEmail(@Param("email") String email);
}
