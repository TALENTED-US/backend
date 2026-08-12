package com.talented.buttie.mydata.mapper;

import com.talented.buttie.mydata.domain.AccountVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AccountMapper {

    List<AccountVO> findActiveByUserId(@Param("userId") Long userId);

    AccountVO findByUserIdAndExternalId(
        @Param("userId") Long userId,
        @Param("externalAccountId") String externalAccountId
    );

    int insert(AccountVO account);

    int update(AccountVO account);

    int deactivateAllByUserId(@Param("userId") Long userId);

    int deactivateByUserIdAndExternalId(
        @Param("userId") Long userId,
        @Param("externalAccountId") String externalAccountId
    );
}
