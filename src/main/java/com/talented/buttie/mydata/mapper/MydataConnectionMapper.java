package com.talented.buttie.mydata.mapper;

import com.talented.buttie.mydata.domain.MydataConnectionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MydataConnectionMapper {

    MydataConnectionVO findByUserIdAndProvider(
        @Param("userId") Long userId,
        @Param("provider") String provider
    );

    int insert(MydataConnectionVO connection);

    int update(MydataConnectionVO connection);
}
