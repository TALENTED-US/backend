package com.talented.buttie.ledger.domain.calendar;

import com.talented.buttie.ledger.domain.ExpenseCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarCategoryExpenseVO {

    private ExpenseCategory category;

    private int amount;
}