package com.talented.buttie.mydata.mapper;

import com.talented.buttie.mydata.domain.CardVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CardMapper {

    List<CardVO> findActiveByUserId(@Param("userId") Long userId);

    CardVO findByUserIdAndExternalId(
        @Param("userId") Long userId,
        @Param("externalCardId") String externalCardId
    );

    int insert(CardVO card);

    int update(CardVO card);

    int deactivateAllByUserId(@Param("userId") Long userId);

    int deactivateByUserIdAndExternalId(
        @Param("userId") Long userId,
        @Param("externalCardId") String externalCardId
    );
}
