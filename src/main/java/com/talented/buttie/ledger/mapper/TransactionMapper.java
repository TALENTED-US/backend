package com.talented.buttie.ledger.mapper;

import com.talented.buttie.ledger.domain.TransactionVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TransactionMapper {
    List<TransactionVO> findAllByUserId(@Param("userId") Long userId);

    int insertTransaction(TransactionVO transaction);

    TransactionVO findById(@Param("transactionId") Long transactionId);

    // 수동 등록 거래 전체 수정 (외부거래 ID가 없을 때 사용)
    int updateTransaction(TransactionVO transaction);

    // 외부 연동 거래 메모 단일 수정 (외부거래 ID가 있을 때 사용)
    int updateTransactionMemo(@Param("transactionId") Long transactionId, @Param("transactionMemo") String transactionMemo);
}
