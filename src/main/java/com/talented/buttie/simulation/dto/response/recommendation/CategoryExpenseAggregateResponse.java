package com.talented.buttie.simulation.dto.response.recommendation;

import com.talented.buttie.ledger.domain.ExpenseCategory;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryExpenseAggregateResponse {

    private ExpenseCategory expenseCategory;
    private BigDecimal totalAmount;
}
