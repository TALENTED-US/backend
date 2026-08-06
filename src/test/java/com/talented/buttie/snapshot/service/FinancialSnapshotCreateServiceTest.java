package com.talented.buttie.snapshot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;

import com.talented.buttie.account.domain.AccountType;
import com.talented.buttie.account.domain.AccountVO;
import com.talented.buttie.account.mapper.AccountMapper;
import com.talented.buttie.ledger.mapper.TransactionMapper;
import com.talented.buttie.snapshot.domain.FinancialSnapshotVO;
import com.talented.buttie.snapshot.domain.RiskLevel;
import com.talented.buttie.snapshot.dto.response.SnapshotTransactionAggregateResponse;
import com.talented.buttie.snapshot.mapper.FinancialSnapshotMapper;
import com.talented.buttie.user.domain.EmploymentPreparationVO;
import com.talented.buttie.user.mapper.EmploymentPreparationMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FinancialSnapshotCreateServiceTest {

    @Mock
    private FinancialSnapshotMapper financialSnapshotMapper;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private EmploymentPreparationMapper employmentPreparationMapper;

    @InjectMocks
    private FinancialSnapshotCreateService financialSnapshotCreateService;

    private Long userId;
    private LocalDate today;
    private LocalDate targetEmploymentDate;

    @BeforeEach
    void setup() {
        userId = 1L;
        today = LocalDate.now();
        targetEmploymentDate = today.plusDays(90);

        willAnswer(invocation -> {
            FinancialSnapshotVO snapshot = invocation.getArgument(0);
            snapshot.setSnapshotId(1L);
            return null;
        }).given(financialSnapshotMapper)
            .save(any(FinancialSnapshotVO.class));
    }

    @Test
    @DisplayName("유동자산 계좌만 합산한다.")
    void liquidAssetsSum() {

        List<AccountVO> accounts = List.of(
            AccountVO.builder()
                .accountType(AccountType.CHECKING)
                .balance(100_000)
                .build(),
            AccountVO.builder()
                .accountType(AccountType.SAVINGS)
                .balance(200_000)
                .build(),
            AccountVO.builder()
                .accountType(AccountType.DEPOSIT)
                .balance(300_000)
                .build(),
            AccountVO.builder()
                .accountType(AccountType.LOAN) // 비유동자산은 제외 확인
                .balance(999_999)
                .build()
        );
        // given
        given(accountMapper.findActiveByUserId(userId))
            .willReturn(accounts);

        given(transactionMapper.aggregateSnapshotTransactions(
            eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)
        )).willReturn(SnapshotTransactionAggregateResponse.empty());

        // when
        FinancialSnapshotVO result = financialSnapshotCreateService.createSnapshot(userId);

        // then
        assertEquals(600_000, result.getLiquidAssets());
        assertEquals(RiskLevel.STABLE, result.getRiskLevel());
        then(financialSnapshotMapper).should()
            .save(any(FinancialSnapshotVO.class));
    }

    @Test
    @DisplayName("최근 3개월 거래 집계로 평균 수입/지출을 계산한다.")
    void recentThreeMonthsAggregate(){

        // given
        given(accountMapper.findActiveByUserId(userId))
            .willReturn(List.of());

        LocalDate fromDate = today.minusMonths(3);
        LocalDate toDate = today.plusDays(1);

        int weekDays = countDaysInPeriod(fromDate, toDate, false);
        int weekendDays = countDaysInPeriod(fromDate, toDate, true);

        SnapshotTransactionAggregateResponse aggregate =
            new SnapshotTransactionAggregateResponse(
                BigDecimal.valueOf(weekDays * 10_000L), // 평일 수입 총합
                BigDecimal.valueOf(weekDays * 20_000L), // 평일 지출 총합
                BigDecimal.valueOf(weekendDays * 5_000L), // 주말 수입 총합
                BigDecimal.valueOf(weekendDays * 30_000L), // 주말 지출 총합
                BigDecimal.valueOf(900_000), // 최근 3개월 수입 총합
                BigDecimal.valueOf(1_200_000) // 최근 3개월 지출 총합
            );

        given(transactionMapper.aggregateSnapshotTransactions(
            eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)
        )).willReturn(aggregate);

        // when
        FinancialSnapshotVO result = financialSnapshotCreateService.createSnapshot(userId);

        // then
        assertEquals(new BigDecimal("10000.00"), result.getAvgWeekIncome());
        assertEquals(new BigDecimal("20000.00"), result.getAvgWeekExpense());
        assertEquals(new BigDecimal("5000.00"), result.getAvgWeekendIncome());
        assertEquals(new BigDecimal("30000.00"), result.getAvgWeekendExpense());

        assertEquals(new BigDecimal("300000.00"), result.getAvgMonthlyIncome());
        assertEquals(new BigDecimal("400000.00"), result.getAvgMonthlyExpense());
        assertEquals(-100_000, result.getMonthlyNetCashflow());
    }

    @Test
    @DisplayName("월 순소진이 0 이하이면 버티는 기간은 null이고 안정상태이다.")
    void surplusIsStable() {
        // given
        given(accountMapper.findActiveByUserId(userId))
            .willReturn(List.of(
                AccountVO.builder()
                    .accountType(AccountType.CHECKING)
                    .balance(1_000_000)
                    .build()
            ));

        SnapshotTransactionAggregateResponse aggregate = aggregateWithDailyAverage(
            30_000, 10_000, 20_000, 10_000, 900_000, 600_000
        );

        given(transactionMapper.aggregateSnapshotTransactions(
            eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)
        )).willReturn(aggregate);

        // when
        FinancialSnapshotVO result = financialSnapshotCreateService.createSnapshot(userId);

        // then
        assertEquals(new BigDecimal("999.99"), result.getCurrentPrepMonths());
        assertEquals(RiskLevel.STABLE, result.getRiskLevel());
    }

    @Test
    @DisplayName("버티는 기간은 전체 유동자산을 월 순소진으로 나누어 계산한다.")
    void calculatePrepPossibleMonths() {
        // given
        given(accountMapper.findActiveByUserId(userId))
            .willReturn(List.of(
                AccountVO.builder()
                    .accountType(AccountType.CHECKING)
                    .balance(1_000_000)
                    .build()
            ));

        int weekDaysInMonth = countDaysInCurrentMonth(today, false);

        SnapshotTransactionAggregateResponse aggregate = aggregateWithDailyAverage(
            0, 250_000 / weekDaysInMonth, 0,
            0, 0, 750_000
            );

        given(transactionMapper.aggregateSnapshotTransactions(
            eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)
        )).willReturn(aggregate);

        given(employmentPreparationMapper.selectEmploymentPreparation(userId))
            .willReturn(employmentPreparation(today.plusDays(365)));

        // when
        FinancialSnapshotVO result = financialSnapshotCreateService.createSnapshot(userId);

        // then
        BigDecimal expectedMonthlyBurn = new BigDecimal("250000.00");

        BigDecimal expectedCurrentPrepMonths = BigDecimal.valueOf(1_000_000)
            .divide(expectedMonthlyBurn, 2, RoundingMode.HALF_UP);

        assertEquals(expectedCurrentPrepMonths, result.getCurrentPrepMonths());
    }

    @Test
    @DisplayName("목표취업일까지 10일 남고 생존일수가 24일이면 안정이다.")
    void riskLevelD10Stable() {
        assertRiskLevel(10, 24, RiskLevel.STABLE);
    }

    @Test
    @DisplayName("목표취업일까지 10일 남고 생존일수가 10일이면 주의이다.")
    void riskLevelD10Caution() {
        assertRiskLevel(10, 10, RiskLevel.CAUTION);
    }

    @Test
    @DisplayName("목표취업일까지 10일 남고 생존일수가 9일이면 위험이다.")
    void riskLevelD10Danger() {
        assertRiskLevel(10, 9, RiskLevel.DANGER);
    }

    @Test
    @DisplayName("목표취업일까지 90일 남고 생존일수가 108일이면 안정이다.")
    void riskLevelD90Stable() {
        assertRiskLevel(90, 108, RiskLevel.STABLE);
    }

    @Test
    @DisplayName("목표취업일까지 90일 남고 생존일수가 90일이면 주의이다.")
    void riskLevelD90Caution() {
        assertRiskLevel(90, 90, RiskLevel.CAUTION);
    }

    @Test
    @DisplayName("목표취업일까지 90일 남고 생존일수가 89일이면 위험이다.")
    void riskLevelD90Danger() {
        assertRiskLevel(90, 89, RiskLevel.DANGER);
    }

    @Test
    @DisplayName("목표취업일까지 365일 남고 생존일수가 425일이면 안정이다.")
    void riskLevelD365Stable() {
        assertRiskLevel(365, 425, RiskLevel.STABLE);
    }

    @Test
    @DisplayName("목표취업일까지 365일 남고 생존일수가 365일이면 주의이다.")
    void riskLevelD365Caution() {
        assertRiskLevel(365, 365, RiskLevel.CAUTION);
    }

    @Test
    @DisplayName("목표취업일까지 365일 남고 생존일수가 364일이면 위험이다.")
    void riskLevelD365Danger() {
        assertRiskLevel(365, 364, RiskLevel.DANGER);
    }

    @Test
    @DisplayName("거래와 계좌가 없어도 0 기준값으로 스냅샷을 생성한다.")
    void createSnapshotWithoutAccountAndTransaction() {
        // given
        given(accountMapper.findActiveByUserId(userId))
            .willReturn(List.of());

        given(transactionMapper.aggregateSnapshotTransactions(
            eq(userId),
            any(LocalDateTime.class),
            any(LocalDateTime.class)
        )).willReturn(SnapshotTransactionAggregateResponse.empty());

        // when
        FinancialSnapshotVO result =
            financialSnapshotCreateService.createSnapshot(userId);

        // then
        assertEquals(0, result.getLiquidAssets());
        assertEquals(new BigDecimal("0.00"),
            result.getAvgWeekIncome());
        assertEquals(new BigDecimal("0.00"),
            result.getAvgWeekExpense());
        assertEquals(new BigDecimal("0.00"),
            result.getAvgWeekendIncome());
        assertEquals(new BigDecimal("0.00"),
            result.getAvgWeekendExpense());
        assertEquals(new BigDecimal("0.00"),
            result.getAvgMonthlyIncome());
        assertEquals(new BigDecimal("0.00"),
            result.getAvgMonthlyExpense());
        assertEquals(0, result.getMonthlyNetCashflow());
        assertEquals(new BigDecimal("999.99"), result.getCurrentPrepMonths());
        assertEquals(RiskLevel.STABLE, result.getRiskLevel());
    }

    private EmploymentPreparationVO employmentPreparation(LocalDate
        targetEmploymentDate) {
        return EmploymentPreparationVO.builder()
            .userId(userId)
            .targetEmploymentDate(targetEmploymentDate)
            .build();
    }

    private SnapshotTransactionAggregateResponse
    aggregateWithDailyAverage(
        int weekIncome,
        int weekExpense,
        int weekendIncome,
        int weekendExpense,
        int monthlyIncomeTotal,
        int monthlyExpenseTotal
    ) {
        LocalDate fromDate = today.minusMonths(3);
        LocalDate toDate = today.plusDays(1);

        int weekDays = countDaysInPeriod(fromDate, toDate, false);
        int weekendDays = countDaysInPeriod(fromDate, toDate, true);

        return new SnapshotTransactionAggregateResponse(
            BigDecimal.valueOf((long) weekIncome * weekDays),
            BigDecimal.valueOf((long) weekExpense * weekDays),
            BigDecimal.valueOf((long) weekendIncome * weekendDays),
            BigDecimal.valueOf((long) weekendExpense * weekendDays),
            BigDecimal.valueOf(monthlyIncomeTotal),
            BigDecimal.valueOf(monthlyExpenseTotal)
        );
    }

    private void assertRiskLevel(
        int remainingDays, int survivalDays, RiskLevel expectedRiskLevel
    ) {
        int monthlyBurn = 300_000;
        int liquidAssets = BigDecimal.valueOf(monthlyBurn)
            .multiply(BigDecimal.valueOf(survivalDays))
            .divide(BigDecimal.valueOf(30), 0, RoundingMode.CEILING)
            .intValue();

        given(accountMapper.findActiveByUserId(userId))
            .willReturn(List.of(
                AccountVO.builder().accountType(AccountType.CHECKING)
                    .balance(liquidAssets)
                    .build()
            ));

        given(transactionMapper.aggregateSnapshotTransactions(
            eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)
        )).willReturn(aggregateWithMonthlyBurn(monthlyBurn));

        given(employmentPreparationMapper.selectEmploymentPreparation(userId))
            .willReturn(employmentPreparation(today.plusDays(remainingDays)));

        FinancialSnapshotVO result = financialSnapshotCreateService.createSnapshot(userId);

        assertEquals(expectedRiskLevel, result.getRiskLevel());
    }

    private SnapshotTransactionAggregateResponse
    aggregateWithMonthlyBurn(int monthlyBurn) {
        int weekDaysInMonth = countDaysInCurrentMonth(today, false);
        int weekDaysInSnapshotPeriod = countDaysInPeriod(today.minusMonths(3), today.plusDays(1), false);
        BigDecimal dailyWeekExpense = BigDecimal.valueOf(monthlyBurn)
            .divide(BigDecimal.valueOf(weekDaysInMonth), 2, RoundingMode.HALF_UP);

        return new SnapshotTransactionAggregateResponse(
            BigDecimal.ZERO,
            dailyWeekExpense.multiply(BigDecimal.valueOf(weekDaysInSnapshotPeriod)),
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.valueOf((long) monthlyBurn * 3)
        );
    }

    private int countDaysInCurrentMonth(LocalDate date, boolean weekend) {
        LocalDate current = date.withDayOfMonth(1);
        LocalDate end = current.plusMonths(1);

        return countDaysInPeriod(current, end, weekend);
    }

    private int countDaysInPeriod(LocalDate fromInclusive, LocalDate
        toExclusive, boolean weekend) {
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
