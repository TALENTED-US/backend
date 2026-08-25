package com.talented.buttie.ledger.service;
import com.talented.buttie.ledger.service.calendar.ReadCalendarMonthlyService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.ledger.domain.TransactionType;
import com.talented.buttie.ledger.domain.calendar.CalendarCategoryExpenseVO;
import com.talented.buttie.ledger.domain.calendar.CalendarMonthlyVO;
import com.talented.buttie.ledger.domain.calendar.CalendarTransactionVO;
import com.talented.buttie.ledger.mapper.CalendarMapper;
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
class ReadCalendarMonthlyServiceTest {

    @Mock
    private CalendarMapper calendarMapper;

    @InjectMocks
    private ReadCalendarMonthlyService readCalendarMonthlyService;

    private Long userId;
    private int year;
    private int month;

    @BeforeEach
    void setUp() {
        userId = 1L;
        year = 2026;
        month = 7;
    }

    @Test
    @DisplayName("성공: 월 요약, 카테고리별 지출, 거래 리스트를 모두 조회한다")
    void getCalendarMonthlySuccess() {
        // given
        List<CalendarTransactionVO> mockTransactions = List.of(
            new CalendarTransactionVO(1L, LocalDateTime.of(2026, 7, 10, 18, 0),
                "알바 급여", ExpenseCategory.OTHER_FINANCE, TransactionType.INCOME, 480000, "KB국민은행"),
            new CalendarTransactionVO(2L, LocalDateTime.of(2026, 7, 1, 9, 0),
                "월세", ExpenseCategory.HOUSING_COMMUNICATION, TransactionType.FIXED, 500000, "KB국민은행"),
            new CalendarTransactionVO(3L, LocalDateTime.of(2026, 7, 20, 10, 15),
                "스벅", ExpenseCategory.FOOD, TransactionType.EXPENSE, 6300, "KB국민은행")
        );

        List<CalendarCategoryExpenseVO> mockCategoryExpenses = List.of(
            new CalendarCategoryExpenseVO(ExpenseCategory.HOUSING_COMMUNICATION, 500000),
            new CalendarCategoryExpenseVO(ExpenseCategory.FOOD, 6300)
        );

        given(calendarMapper.selectTransactionsByMonth(userId, year, month))
            .willReturn(mockTransactions);
        given(calendarMapper.selectCategoryExpensesByMonth(userId, year, month))
            .willReturn(mockCategoryExpenses);

        // when
        CalendarMonthlyVO result = readCalendarMonthlyService.getCalendarMonthly(userId, year, month);

        // then
        assertEquals(480000, result.getTotalIncome());
        assertEquals(506300, result.getTotalExpense());  // 500000 + 6300
        assertEquals(-26300, result.getNetCashFlow());   // 480000 - 506300
        assertEquals(2, result.getCategoryExpenses().size());
        assertEquals(3, result.getTransactions().size());
    }

    @Test
    @DisplayName("성공: 해당 월에 거래가 없으면 총합은 0이고 리스트는 비어있다")
    void getCalendarMonthlyWithNoTransactions() {
        // given
        given(calendarMapper.selectTransactionsByMonth(userId, year, month))
            .willReturn(List.of());
        given(calendarMapper.selectCategoryExpensesByMonth(userId, year, month))
            .willReturn(List.of());

        // when
        CalendarMonthlyVO result = readCalendarMonthlyService.getCalendarMonthly(userId, year, month);

        // then
        assertEquals(0, result.getTotalIncome());
        assertEquals(0, result.getTotalExpense());
        assertEquals(0, result.getNetCashFlow());
        assertTrue(result.getCategoryExpenses().isEmpty());
        assertTrue(result.getTransactions().isEmpty());
    }

    @Test
    @DisplayName("성공: 지출 총합은 EXPENSE와 FIXED 타입의 합계다")
    void totalExpenseIncludesBothExpenseAndFixed() {
        // given
        List<CalendarTransactionVO> mockTransactions = List.of(
            new CalendarTransactionVO(1L, LocalDateTime.of(2026, 7, 1, 9, 0),
                "월세", ExpenseCategory.HOUSING_COMMUNICATION, TransactionType.FIXED, 500000, "KB국민은행"),
            new CalendarTransactionVO(2L, LocalDateTime.of(2026, 7, 20, 10, 15),
                "스벅", ExpenseCategory.FOOD, TransactionType.EXPENSE, 6300, "KB국민은행")
        );

        given(calendarMapper.selectTransactionsByMonth(userId, year, month))
            .willReturn(mockTransactions);
        given(calendarMapper.selectCategoryExpensesByMonth(userId, year, month))
            .willReturn(List.of());

        // when
        CalendarMonthlyVO result = readCalendarMonthlyService.getCalendarMonthly(userId, year, month);

        // then
        assertEquals(0, result.getTotalIncome());
        assertEquals(506300, result.getTotalExpense());  // FIXED 500000 + EXPENSE 6300
    }

    @Test
    @DisplayName("성공: INCOME 타입은 수입 총합에만 포함된다")
    void totalIncomeIncludesOnlyIncomeType() {
        // given
        List<CalendarTransactionVO> mockTransactions = List.of(
            new CalendarTransactionVO(1L, LocalDateTime.of(2026, 7, 10, 18, 0),
                "알바 급여", ExpenseCategory.OTHER_FINANCE, TransactionType.INCOME, 480000, "KB국민은행"),
            new CalendarTransactionVO(2L, LocalDateTime.of(2026, 7, 20, 10, 15),
                "스벅", ExpenseCategory.FOOD, TransactionType.EXPENSE, 6300, "KB국민은행")
        );

        given(calendarMapper.selectTransactionsByMonth(userId, year, month))
            .willReturn(mockTransactions);
        given(calendarMapper.selectCategoryExpensesByMonth(userId, year, month))
            .willReturn(List.of());

        // when
        CalendarMonthlyVO result = readCalendarMonthlyService.getCalendarMonthly(userId, year, month);

        // then
        assertEquals(480000, result.getTotalIncome());
        assertEquals(6300, result.getTotalExpense());
        assertEquals(473700, result.getNetCashFlow());
    }
}