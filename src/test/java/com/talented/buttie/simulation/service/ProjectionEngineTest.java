package com.talented.buttie.simulation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.talented.buttie.simulation.domain.MonthlyProjectionVO;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProjectionEngineTest {

    private ProjectionEngine projectionEngine;
    private Long simulationId;
    private LocalDate simulationStartDate;
    private LocalDate simulationDueDate;
    private List<MonthlyProjectionVO> existingProjections;

    @BeforeEach
    void setUp() {
        projectionEngine = new ProjectionEngine();
        simulationId = 10L;
        simulationStartDate = LocalDate.of(2026, 8, 1);
        simulationDueDate = LocalDate.of(2026, 10, 31);
        existingProjections = List.of(
            projection(LocalDate.of(2026, 8, 1), 1_000_000),
            projection(LocalDate.of(2026, 9, 1), 2_000_000),
            projection(LocalDate.of(2026, 10, 1), 3_000_000)
        );
    }

    @Test
    @DisplayName("스냅샷 기준으로 최초 예상 재정 계획을 생성한다.")
    void createInitialProjections() {
        List<MonthlyProjectionVO> result = projectionEngine.createInitialProjections(
            simulationId, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 10, 31),
            5_000_000, 1_000_000, 3_000_000, 2_000_000
        );
        assertEquals(3, result.size());
        assertEquals(LocalDate.of(2026, 8, 1), result.get(0).getProjectionMonth());

        assertEquals(5_000_000, result.get(0).getOpeningBalance());
        assertEquals(1_000_000, result.get(0).getExpectedIncome());
        assertEquals(3_000_000, result.get(0).getExpectedExpense());
        assertEquals(3_000_000, result.get(0).getClosingBalance());
        assertFalse(result.get(0).getAdjustmentRequired());

        assertEquals(3_000_000, result.get(1).getOpeningBalance());
        assertEquals(1_000_000, result.get(1).getClosingBalance());
        assertTrue(result.get(1).getAdjustmentRequired());

        assertEquals(1_000_000, result.get(2).getOpeningBalance());
        assertEquals(-1_000_000, result.get(2).getClosingBalance());
        assertTrue(result.get(2).getAdjustmentRequired());
    }

    @Test
    @DisplayName("시작일부터 종료일까지 월 목록을 생성한다.")
    void createProjectionMonthsFromStartToDueDate() {
        List<MonthlyProjectionVO> result = projectionEngine.recalculateProjections(
            simulationId, simulationStartDate, simulationDueDate, existingProjections, 1_000_000
        );
        List<LocalDate> projectionMonths = result.stream()
            .map(MonthlyProjectionVO::getProjectionMonth)
            .toList();
        assertEquals(
            List.of(
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 1)
            ),
            projectionMonths
        );
    }

    @Test
    @DisplayName("기간을 축소하면 Projection 개수가 감소한다.")
    void reduceProjectionCountWhenPeriodIsShortened() {
        LocalDate shortenedStartDate = LocalDate.of(2026, 9, 1);
        LocalDate shortenedDueDate = LocalDate.of(2026, 10, 5);

        List<MonthlyProjectionVO> result = projectionEngine.recalculateProjections(
            simulationId, shortenedStartDate, shortenedDueDate, existingProjections, 1_000_000
        );

        assertEquals(2, result.size());
        assertEquals(
            LocalDate.of(2026, 10, 1),
            result.get(result.size() - 1).getProjectionMonth()
        );
        assertEquals(2_000_000, result.get(0).getOpeningBalance());
    }

    @Test
    @DisplayName("기간을 확장하면 Projection 개수가 증가한다.")
    void increaseProjectionCountWhenPeriodIsExtended() {
        LocalDate extendedStartDate = LocalDate.of(2026, 7, 15);
        LocalDate extendedDueDate = LocalDate.of(2026, 12, 5);

        List<MonthlyProjectionVO> result = projectionEngine.recalculateProjections(
            simulationId, extendedStartDate, extendedDueDate, existingProjections, 1_000_000
        );

        assertEquals(6, result.size());
        assertEquals(LocalDate.of(2026, 7, 1),
                    result.get(0).getProjectionMonth());
        assertEquals(LocalDate.of(2026, 12, 1),
                    result.get(result.size() - 1).getProjectionMonth());
    }

    @Test
    @DisplayName("월 경계와 종료일이 속한 마지막 월을 포함한다.")
    void includeLastMonthAcrossMonthBoundary() {
        LocalDate boundaryStartDate = LocalDate.of(2026, 8, 31);
        LocalDate boundaryDueDate = LocalDate.of(2026, 10, 1);

        List<MonthlyProjectionVO> result = projectionEngine.recalculateProjections(
            simulationId, boundaryStartDate, boundaryDueDate, existingProjections, 1_000_000
        );

        List<LocalDate> projectionMonths = result.stream()
            .map(MonthlyProjectionVO::getProjectionMonth)
            .toList();

        assertTrue(projectionMonths.contains(LocalDate.of(2026, 10, 1)));
    }

    private MonthlyProjectionVO projection(
        LocalDate projectionMonth,
        int openingBalance
    ) {
        return MonthlyProjectionVO.builder()
            .simulationId(simulationId)
            .projectionMonth(projectionMonth)
            .openingBalance(openingBalance)
            .expectedIncome(3_000_000)
            .expectedExpense(2_000_000)
            .closingBalance(openingBalance + 1_000_000)
            .adjustmentRequired(false)
            .build();
    }
}
