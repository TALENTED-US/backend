package com.talented.buttie.ledger.mapper;

import com.talented.buttie.ledger.domain.TransactionVO;
import com.talented.buttie.snapshot.dto.response.SnapshotTransactionAggregateResponse;
import java.time.LocalDateTime;
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

    // 수동 등록 거래 삭제
    int deleteTransaction(@Param("transactionId") Long transactionId);

    SnapshotTransactionAggregateResponse aggregateSnapshotTransactions(
        @Param("userId") Long userId,
        @Param("fromDateTime") LocalDateTime fromDateTime,
        @Param("toDateTime") LocalDateTime toDateTime
    );
    // 고정 지출 상세 목록 조회 (TransactionType이 FIXED인 항목)
    List<TransactionVO> findFixedExpensesByUserId(@Param("userId") Long userId);
}
