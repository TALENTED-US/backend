package com.talented.buttie.ledger.service.calendar;

import com.talented.buttie.ledger.domain.TransactionType;
import com.talented.buttie.ledger.domain.calendar.CalendarCategoryExpenseVO;
import com.talented.buttie.ledger.domain.calendar.CalendarMonthlyVO;
import com.talented.buttie.ledger.domain.calendar.CalendarTransactionVO;
import com.talented.buttie.ledger.mapper.CalendarMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReadCalendarMonthlyService {

    private final CalendarMapper calendarMapper;

    public CalendarMonthlyVO getCalendarMonthly(Long userId, int year, int month) {

        List<CalendarTransactionVO> transactions =
            calendarMapper.selectTransactionsByMonth(userId, year, month);

        List<CalendarCategoryExpenseVO> categoryExpenses =
            calendarMapper.selectCategoryExpensesByMonth(userId, year, month);

        int totalIncome = transactions.stream()
            .filter(t -> t.getTransactionType() == TransactionType.INCOME)
            .mapToInt(CalendarTransactionVO::getAmount)
            .sum();

        int totalExpense = transactions.stream()
            .filter(t -> t.getTransactionType() == TransactionType.EXPENSE
                || t.getTransactionType() == TransactionType.FIXED)
            .mapToInt(CalendarTransactionVO::getAmount)
            .sum();

        int netCashFlow = totalIncome - totalExpense;

        return new CalendarMonthlyVO(
            totalIncome,
            totalExpense,
            netCashFlow,
            categoryExpenses,
            transactions
        );
    }
}