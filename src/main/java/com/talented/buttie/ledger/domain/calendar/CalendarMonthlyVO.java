package com.talented.buttie.ledger.domain.calendar;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarMonthlyVO {


    private int totalIncome;

    private int totalExpense;

    private int netCashFlow;

    private List<CalendarCategoryExpenseVO> categoryExpenses;

    private List<CalendarTransactionVO> transactions;
}