package com.talented.buttie.simulation.service;

import com.talented.buttie.catalog.domain.PolicyVO;
import com.talented.buttie.catalog.mapper.PolicyMapper;
import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import com.talented.buttie.mydata.domain.AccountType;
import com.talented.buttie.mydata.domain.AccountVO;
import com.talented.buttie.mydata.mapper.AccountMapper;
import com.talented.buttie.simulation.domain.FinancialSnapshotVO;
import com.talented.buttie.simulation.domain.MonthlyProjectionVO;
import com.talented.buttie.simulation.domain.RiskLevel;
import com.talented.buttie.simulation.domain.SimulationItemCategory;
import com.talented.buttie.simulation.domain.SimulationItemVO;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.dto.response.simulation.SimulationItemReportResponse;
import com.talented.buttie.simulation.dto.response.simulation.SimulationItemResponse;
import com.talented.buttie.simulation.dto.response.simulation.SimulationItemsByCategoryResponse;
import com.talented.buttie.simulation.dto.response.snapshot.SnapshotTransactionAggregateResponse;
import com.talented.buttie.simulation.exception.AnalysisErrorCode;
import com.talented.buttie.simulation.exception.SimulationErrorCode;
import com.talented.buttie.simulation.mapper.FinancialSnapshotMapper;
import com.talented.buttie.simulation.mapper.SimulationItemMapper;
import com.talented.buttie.simulation.mapper.SimulationMapper;
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
public class SimulationItemReadService {

    private final SimulationMapper simulationMapper;
    private final SimulationItemMapper simulationItemMapper;
    private final FinancialSnapshotMapper financialSnapshotMapper;
    private final SimulationItemCalculationService simulationItemCalculationService;
    private final PolicyMapper policyMapper;

    @Transactional(readOnly = true)
    public SimulationItemReportResponse getAppliedItemReport(Long userId) {
        SimulationVO simulation = simulationMapper.findActiveByUserId(userId);

        if (simulation == null) {
            throw ApplicationException.from(SimulationErrorCode.NOT_CONFIRMED_SIMULATION_NOT_FOUND);
        }

        FinancialSnapshotVO snapshot = financialSnapshotMapper.findLatestByUserId(userId);

        if (snapshot == null) {
            throw ApplicationException.from(AnalysisErrorCode.SNAPSHOT_NOT_FOUND);
        }

        List<SimulationItemVO> appliedItems =
            simulationItemMapper.findAllActiveBySimulationId(simulation.getSimulationId());

        List<MonthlyProjectionVO> beforeProjections =
            simulationItemCalculationService.createBaselineProjections(simulation, snapshot);

        List<MonthlyProjectionVO> afterProjections =
            simulationItemCalculationService.recalculateProjections(simulation, snapshot, appliedItems);

        return simulationItemCalculationService.createReportResponse(
            userId,
            snapshot,
            appliedItems,
            beforeProjections,
            afterProjections
        );
    }

    @Transactional(readOnly = true)
    public SimulationItemsByCategoryResponse findItemListByCategory(Long userId, SimulationItemCategory itemCategory) {
        SimulationVO simulation = simulationMapper.findActiveByUserId(userId);

        if (simulation == null) {
            throw ApplicationException.from(SimulationErrorCode.NOT_CONFIRMED_SIMULATION_NOT_FOUND);
        }

        List<SimulationItemResponse> items = simulationItemMapper
            .findAllByCategory(simulation.getSimulationId(), itemCategory)
            .stream()
            .map(item -> {
                PolicyVO policy = item.getPolicy() != null
                    ? item.getPolicy()
                    : (item.getPolicyId() != null ? policyMapper.findById(item.getPolicyId()) : null);
                return SimulationItemResponse.from(item, policy, simulation);
            })
            .toList();

        return new SimulationItemsByCategoryResponse(items);
    }

    @Transactional(readOnly = true)
    public List<SimulationItemResponse> findAllAppliedItems(SimulationVO simulation) {
        return simulationItemMapper.findAllActiveBySimulationId(simulation.getSimulationId())
            .stream()
            .map(item -> {
                PolicyVO policy = item.getPolicy() != null
                    ? item.getPolicy()
                    : (item.getPolicyId() != null ? policyMapper.findById(item.getPolicyId()) : null);
                return SimulationItemResponse.from(item, policy, simulation);
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal getCurrentPrepMonths(Long userId) {
        FinancialSnapshotVO snapshot = financialSnapshotMapper.findLatestByUserId(userId);

        if (snapshot == null) {
            throw ApplicationException.from(AnalysisErrorCode.SNAPSHOT_NOT_FOUND);
        }

        return snapshot.getCurrentPrepMonths();
    }

    @Service
    @RequiredArgsConstructor
    public static class FinancialSnapshotCreateService {

        private static final int SCALE = 2;
        private static final BigDecimal DAYS_IN_MONTH = new BigDecimal("30");
        private static final BigDecimal MAX_CURRENT_PREP_MONTHS = new BigDecimal("999.99");
        private static final Set<AccountType> LIQUID_ACCOUNT_TYPES = Set.of(AccountType.CHECKING, AccountType.SAVINGS, AccountType.DEPOSIT);
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

            int weekDaysInSnapshotPeriod = countDaysInPeriod(fromDateTime.toLocalDate(), toDateTime.toLocalDate(), false);
            int weekendDaysInSnapshotPeriod = countDaysInPeriod(fromDateTime.toLocalDate(), toDateTime.toLocalDate(), true);

            BigDecimal avgWeekIncome = divide(aggregate.weekIncomeTotalOrZero(), weekDaysInSnapshotPeriod);
            BigDecimal avgWeekExpense = divide(aggregate.weekExpenseTotalOrZero(), weekDaysInSnapshotPeriod);
            BigDecimal avgWeekendIncome = divide(aggregate.weekendIncomeTotalOrZero(), weekendDaysInSnapshotPeriod);
            BigDecimal avgWeekendExpense = divide(aggregate.weekendExpenseTotalOrZero(), weekendDaysInSnapshotPeriod);
            BigDecimal avgMonthlyIncome = divide(aggregate.monthlyIncomeTotalOrZero(), SNAPSHOT_MONTH_RANGE);
            BigDecimal avgMonthlyExpense = divide(aggregate.monthlyExpenseTotalOrZero(), SNAPSHOT_MONTH_RANGE);

            int monthlyNetCashflow = avgMonthlyIncome.subtract(avgMonthlyExpense).intValue();

            BigDecimal monthlyBurn = avgMonthlyExpense.subtract(avgMonthlyIncome);

            BigDecimal currentPrepMonths = calculateCurrentPrepMonths(liquidAssets, monthlyBurn);
            BigDecimal survivalDays = calculateSurvivalDays(liquidAssets, monthlyBurn);

            RiskLevel riskLevel = resolveRiskLevel(userId, today, monthlyBurn, survivalDays);

            FinancialSnapshotVO snapshot = FinancialSnapshotVO.builder()
                .userId(userId)
                .snapshotBaseDate(today)
                .liquidAssets(liquidAssets)
                .monthlyNetCashflow(monthlyNetCashflow)
                .currentPrepMonths(currentPrepMonths)
                .avgMonthlyExpense(avgMonthlyExpense)
                .avgMonthlyIncome(avgMonthlyIncome)
                .avgWeekendExpense(avgWeekendExpense)
                .avgWeekendIncome(avgWeekendIncome)
                .avgWeekExpense(avgWeekExpense)
                .avgWeekIncome(avgWeekIncome)
                .riskLevel(riskLevel)
                .build();

            financialSnapshotMapper.save(snapshot);

            return snapshot;
        }

        private int calculateLiquidAssets(List<AccountVO> accounts) {
            if (accounts == null || accounts.isEmpty()) {
                return 0;
            }

            return accounts.stream()
                .filter(account -> account.getAccountType() != null)
                .filter(account -> LIQUID_ACCOUNT_TYPES.contains(account.getAccountType()))
                .map(AccountVO::getBalance)
                .filter(balance -> balance != null)
                .mapToInt(Integer::intValue)
                .sum();
        }

        private BigDecimal divide(BigDecimal value, Integer divisor) {
            if (value == null || divisor == null || divisor == 0) {
                return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
            }
            return value.divide(BigDecimal.valueOf(divisor), SCALE, RoundingMode.HALF_UP);
        }

        private int valueOf(Integer value) {
            return value == null ? 0 : value;
        }

        private BigDecimal calculateCurrentPrepMonths(int liquidAssets, BigDecimal monthlyBurn) {
            if (monthlyBurn.compareTo(BigDecimal.ZERO) <= 0) {
                return MAX_CURRENT_PREP_MONTHS;
            }

            BigDecimal currentPrepMonths = BigDecimal.valueOf(liquidAssets)
                .divide(monthlyBurn, SCALE, RoundingMode.HALF_UP);

            if (currentPrepMonths.compareTo(MAX_CURRENT_PREP_MONTHS) > 0) {
                return MAX_CURRENT_PREP_MONTHS;
            }

            return currentPrepMonths;
        }

        private BigDecimal calculateSurvivalDays(int liquidAssets, BigDecimal monthlyBurn) {
            if (monthlyBurn.compareTo(BigDecimal.ZERO) <= 0) {
                return null;
            }

            return BigDecimal.valueOf(liquidAssets)
                .multiply(DAYS_IN_MONTH)
                .divide(monthlyBurn, SCALE, RoundingMode.HALF_UP);
        }

        private RiskLevel resolveRiskLevel(Long userId, LocalDate today, BigDecimal monthlyBurn, BigDecimal survivalDays) {
            if (monthlyBurn.compareTo(BigDecimal.ZERO) <= 0) {
                return RiskLevel.STABLE;
            }

            EmploymentPreparationVO employmentPreparation = employmentPreparationMapper.selectEmploymentPreparation(userId);

            if (employmentPreparation == null
                || employmentPreparation.getTargetEmploymentDate() == null
                || !employmentPreparation.getTargetEmploymentDate().isAfter(today)) {
                return RiskLevel.DANGER;
            }

            long remainingDays = ChronoUnit.DAYS.between(today, employmentPreparation.getTargetEmploymentDate());

            long survivalDaysForRisk = survivalDays
                .setScale(0, RoundingMode.DOWN)
                .longValue();
            long stableMarginDays = calculateStableMarginDays(remainingDays);

            long stableBoundary = remainingDays + stableMarginDays;

            if (survivalDaysForRisk >= stableBoundary) {
                return RiskLevel.STABLE;
            }
            if (survivalDaysForRisk >= remainingDays) {
                return RiskLevel.CAUTION;
            }
            return RiskLevel.DANGER;
        }

        private long calculateStableMarginDays(long remainingDays) {
            long twentyPrecent = BigDecimal.valueOf(remainingDays)
                .multiply(new BigDecimal("0.2"))
                .setScale(0, RoundingMode.CEILING)
                .longValue();

            return Math.min(Math.max(twentyPrecent, 14), 60);
        }

        private int countDaysInPeriod(LocalDate fromInclusive, LocalDate toExclusive, boolean weekend) {
            LocalDate current = fromInclusive;

            int count = 0;

            while (current.isBefore(toExclusive)) {
                boolean currentIsWeekend = isWeekend(current.getDayOfWeek());
                if (currentIsWeekend == weekend) {
                    count++;
                }
                current = current.plusDays(1);
            }
            return count;
        }

        private boolean isWeekend(DayOfWeek dayOfWeek) {
            return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
        }
    }
}
