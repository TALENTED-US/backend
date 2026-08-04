package com.talented.buttie.account.mapper;

import com.talented.buttie.account.domain.AccountVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AccountMapper {
    List<AccountVO> findActiveByUserId(@Param("userId") Long userId);
}
