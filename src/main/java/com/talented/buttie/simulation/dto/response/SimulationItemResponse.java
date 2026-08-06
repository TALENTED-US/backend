package com.talented.buttie.simulation.dto.response;

import com.talented.buttie.catalog.domain.PolicyVO;
import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.simulation.domain.SimulationItemCategory;
import com.talented.buttie.simulation.domain.SimulationItemVO;
import com.talented.buttie.simulation.domain.SimulationRecurrenceType;
import com.talented.buttie.simulation.domain.SimulationVO;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record SimulationItemResponse(

    @ApiModelProperty(value = "암호화된 시뮬레이션 항목 ID")
    String itemId,

    @ApiModelProperty(value = "시뮬레이션 항목 카테고리", example = "EXPENSE")
    SimulationItemCategory itemCategory,

    @ApiModelProperty(value = "시뮬레이션 항목 이름", example = "식비 줄이기")
    String displayName,

    @ApiModelProperty(value = "지출 항목 카테고리(지출 카테고리만 해당)", example = "FOOD")
    ExpenseCategory expenseCategory,

    @ApiModelProperty(value = "항목 적용 금액", example = "100000")
    int amount,

    @ApiModelProperty(value = "항목 적용 시작알", example = "2026-09-01")
    LocalDate applyStartDate,

    @ApiModelProperty(value = "항목 적용 종료일", example = "2026-12-01")
    LocalDate applyEndDate,

    @ApiModelProperty(value = "반복 여부", example = "ONCE")
    SimulationRecurrenceType recurrenceType,

    @ApiModelProperty(value = "정책 정보(정책 카테고리만 해당)")
    PolicySummaryResponse policy
) {
    public static SimulationItemResponse from(
        SimulationItemVO item,
        PolicyVO policy,
        SimulationVO simulation
    ) {
        return SimulationItemResponse.builder()
            .itemId(PKCrypto.encrypt(item.getSimulationItemId()))
            .itemCategory(item.getSimulationItemCategory())
            .displayName(resolveDisplayName(item, policy))
            .expenseCategory(item.getSimulationItemExpenseCategory())
            .amount(item.getSimulationItemApplyAmount())
            .applyStartDate(item.getApplyStartDate())
            .applyEndDate(item.getSimulationItemCategory() == SimulationItemCategory.POLICY
                ? null
                : item.getApplyEndDate())
            .recurrenceType(resolveRecurrenceType(item, policy))
            .policy(PolicySummaryResponse.from(item, policy, simulation))
            .build();
    }

    private static String resolveDisplayName(SimulationItemVO item, PolicyVO policy) {
        return switch (item.getSimulationItemCategory()) {
            case INCOME -> item.getSimulationItemName();
            case EXPENSE -> item.getSimulationItemExpenseCategory() == null
                ? item.getSimulationItemName()
                : item.getSimulationItemExpenseCategory().getValue() + " 줄이기";
            case POLICY -> policy == null
                ? item.getSimulationItemName()
                : policy.getPolicyName();
        };
    }

    private static SimulationRecurrenceType resolveRecurrenceType(SimulationItemVO item, PolicyVO policy) {
        if(item.getSimulationItemCategory() != SimulationItemCategory.POLICY)
            return item.getRecurrenceType();

        int supportMonthCount = (policy == null || policy.getSupportMonthCount() == null)
            ? 1
            : Math.max(policy.getSupportMonthCount(), 1);

        return supportMonthCount == 1
            ? SimulationRecurrenceType.ONCE
            : SimulationRecurrenceType.MONTHLY;
    }
}
