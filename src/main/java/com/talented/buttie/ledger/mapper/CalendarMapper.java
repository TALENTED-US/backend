package com.talented.buttie.ledger.mapper;

import com.talented.buttie.ledger.domain.calendar.CalendarCategoryExpenseVO;
import com.talented.buttie.ledger.domain.calendar.CalendarTransactionVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CalendarMapper {

    List<CalendarTransactionVO> selectTransactionsByMonth(
        @Param("userId") Long userId,
        @Param("year") int year,
        @Param("month") int month
    );

    List<CalendarCategoryExpenseVO> selectCategoryExpensesByMonth(
        @Param("userId") Long userId,
        @Param("year") int year,
        @Param("month") int month
    );
}