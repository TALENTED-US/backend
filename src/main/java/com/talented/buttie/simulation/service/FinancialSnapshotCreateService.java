package com.talented.buttie.simulation.service;

import com.talented.buttie.ledger.mapper.TransactionMapper;
import com.talented.buttie.mydata.domain.AccountType;
import com.talented.buttie.mydata.domain.AccountVO;
import com.talented.buttie.mydata.mapper.AccountMapper;
import com.talented.buttie.simulation.domain.FinancialSnapshotVO;
import com.talented.buttie.simulation.domain.RiskLevel;
import com.talented.buttie.simulation.dto.response.snapshot.SnapshotTransactionAggregateResponse;
import com.talented.buttie.simulation.mapper.FinancialSnapshotMapper;
import com.talented.buttie.user.domain.EmploymentPreparationVO;
import com.talented.buttie.user.mapper.EmploymentPreparationMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FinancialSnapshotCreateService {

    private static final int SCALE = 2;
    private static final BigDecimal DAYS_IN_MONTH = new BigDecimal("30");
    private static final BigDecimal MAX_CURRENT_PREP_MONTHS = new BigDecimal("999.99");
    private static final Set<AccountType> LIQUID_ACCOUNT_TYPES = Set.of(
        AccountType.CHECKING, AccountType.SAVINGS, AccountType.DEPOSIT
    );
    private static final int SNAPSHOT_MONTH_RANGE = 3;

    private final FinancialSnapshotMapper financialSnapshotMapper;
    private final AccountMapper accountMapper;
    private final TransactionMapper transactionMapper;
    private final EmploymentPreparationMapper employmentPreparationMapper;

    @Transactional
    public FinancialSnapshotVO createSnapshot(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime fromDateTime = today.minusMonths(3).atStartOfDay();
        LocalDateTime toDateTime = today.plusDays(1).atStartOfDay();
        int liquidAssets = calculateLiquidAssets(accountMapper.findActiveByUserId(userId));
        SnapshotTransactionAggregateResponse aggregate = transactionMapper.aggregateSnapshotTransactions(
            userId, fromDateTime, toDateTime
        );
        if (aggregate == null) {
            aggregate = SnapshotTransactionAggregateResponse.empty();
        }

        int weekDays = countDaysInPeriod(fromDateTime.toLocalDate(), toDateTime.toLocalDate(), false);
        int weekendDays = countDaysInPeriod(fromDateTime.toLocalDate(), toDateTime.toLocalDate(), true);
        BigDecimal avgWeekIncome = divide(aggregate.weekIncomeTotalOrZero(), weekDays);
        BigDecimal avgWeekExpense = divide(aggregate.weekExpenseTotalOrZero(), weekDays);
        BigDecimal avgWeekendIncome = divide(aggregate.weekendIncomeTotalOrZero(), weekendDays);
        BigDecimal avgWeekendExpense = divide(aggregate.weekendExpenseTotalOrZero(), weekendDays);
        BigDecimal avgMonthlyIncome = divide(aggregate.monthlyIncomeTotalOrZero(), SNAPSHOT_MONTH_RANGE);
        BigDecimal avgMonthlyExpense = divide(aggregate.monthlyExpenseTotalOrZero(), SNAPSHOT_MONTH_RANGE);
        BigDecimal monthlyBurn = avgMonthlyExpense.subtract(avgMonthlyIncome);
        BigDecimal survivalDays = calculateSurvivalDays(liquidAssets, monthlyBurn);

        FinancialSnapshotVO snapshot = FinancialSnapshotVO.builder()
            .userId(userId)
            .snapshotBaseDate(today)
            .liquidAssets(liquidAssets)
            .monthlyNetCashflow(avgMonthlyIncome.subtract(avgMonthlyExpense).intValue())
            .currentPrepMonths(calculateCurrentPrepMonths(liquidAssets, monthlyBurn))
            .avgMonthlyExpense(avgMonthlyExpense)
            .avgMonthlyIncome(avgMonthlyIncome)
            .avgWeekendExpense(avgWeekendExpense)
            .avgWeekendIncome(avgWeekendIncome)
            .avgWeekExpense(avgWeekExpense)
            .avgWeekIncome(avgWeekIncome)
            .riskLevel(resolveRiskLevel(userId, today, monthlyBurn, survivalDays))
            .build();
        financialSnapshotMapper.save(snapshot);
        return snapshot;
    }

    private int calculateLiquidAssets(List<AccountVO> accounts) {
        if (accounts == null || accounts.isEmpty()) return 0;
        return accounts.stream()
            .filter(account -> account.getAccountType() != null)
            .filter(account -> LIQUID_ACCOUNT_TYPES.contains(account.getAccountType()))
            .map(AccountVO::getBalance)
            .filter(balance -> balance != null)
            .mapToInt(Integer::intValue)
            .sum();
    }

    private BigDecimal divide(BigDecimal value, int divisor) {
        if (value == null || divisor == 0) return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        return value.divide(BigDecimal.valueOf(divisor), SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateCurrentPrepMonths(int liquidAssets, BigDecimal monthlyBurn) {
        if (monthlyBurn.compareTo(BigDecimal.ZERO) <= 0) return MAX_CURRENT_PREP_MONTHS;
        return BigDecimal.valueOf(liquidAssets)
            .divide(monthlyBurn, SCALE, RoundingMode.HALF_UP)
            .min(MAX_CURRENT_PREP_MONTHS);
    }

    private BigDecimal calculateSurvivalDays(int liquidAssets, BigDecimal monthlyBurn) {
        if (monthlyBurn.compareTo(BigDecimal.ZERO) <= 0) return null;
        return BigDecimal.valueOf(liquidAssets).multiply(DAYS_IN_MONTH)
            .divide(monthlyBurn, SCALE, RoundingMode.HALF_UP);
    }

    private RiskLevel resolveRiskLevel(Long userId, LocalDate today, BigDecimal monthlyBurn, BigDecimal survivalDays) {
        if (monthlyBurn.compareTo(BigDecimal.ZERO) <= 0) return RiskLevel.STABLE;
        EmploymentPreparationVO preparation = employmentPreparationMapper.selectEmploymentPreparation(userId);
        if (preparation == null || preparation.getTargetEmploymentDate() == null
            || !preparation.getTargetEmploymentDate().isAfter(today)) return RiskLevel.DANGER;

        long remainingDays = ChronoUnit.DAYS.between(today, preparation.getTargetEmploymentDate());
        long survivalDaysForRisk = survivalDays.setScale(0, RoundingMode.DOWN).longValue();
        long stableMarginDays = Math.min(Math.max(
            BigDecimal.valueOf(remainingDays).multiply(new BigDecimal("0.2"))
                .setScale(0, RoundingMode.CEILING).longValue(), 14
        ), 60);
        if (survivalDaysForRisk >= remainingDays + stableMarginDays) return RiskLevel.STABLE;
        return survivalDaysForRisk >= remainingDays ? RiskLevel.CAUTION : RiskLevel.DANGER;
    }

    private int countDaysInPeriod(LocalDate fromInclusive, LocalDate toExclusive, boolean weekend) {
        int count = 0;
        for (LocalDate current = fromInclusive; current.isBefore(toExclusive); current = current.plusDays(1)) {
            boolean isWeekend = current.getDayOfWeek() == DayOfWeek.SATURDAY
                || current.getDayOfWeek() == DayOfWeek.SUNDAY;
            if (isWeekend == weekend) count++;
        }
        return count;
    }
}
