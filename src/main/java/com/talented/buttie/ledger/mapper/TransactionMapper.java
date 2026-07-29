package com.talented.buttie.ledger.mapper;

import com.talented.buttie.ledger.domain.TransactionVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TransactionMapper {
    List<TransactionVO> findAllByUserId(@Param("userId") Long userId);
}

