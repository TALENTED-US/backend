package com.talented.buttie.quest.domain;

import com.talented.buttie.catalog.domain.PolicyVO;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.simulation.domain.SimulationItemCategory;
import com.talented.buttie.simulation.domain.SimulationItemVO;
import com.talented.buttie.simulation.domain.SimulationRecurrenceType;
import com.talented.buttie.simulation.dto.response.SimulationItemResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class QuestVO {
    private Long questId;
    private Long userId;
    private Long simulationId;
    private Long simulationItemId;
    private Long transactionId;
    private LocalDateTime questDeadline;
    private QuestStatus questStatus;
    private String questUrl;
    private Integer expReward;
    private LocalDateTime questCompletedAt;

    // SIMULATION_ITEM & POLICY JOIN 필드
    private String displayName;
    private SimulationItemCategory simulationItemCategory;
    private ExpenseCategory simulationItemExpenseCategory;
    private Integer simulationItemApplyAmount;
    private Long policyId;
    private SimulationRecurrenceType recurrenceType;
    private String policyName;
    private String policyUrl;
    private LocalDate policyDueDate;

    public static QuestVO createFromSimulationItem(
        Long userId,
        Long simulationId,
        SimulationItemVO item,
        PolicyVO policy
    ) {
        SimulationItemCategory category = item.getSimulationItemCategory();
        String name = SimulationItemResponse.resolveDisplayName(item, policy);
        SimulationRecurrenceType recurrence = SimulationItemResponse.resolveRecurrenceType(item, policy);
        Long pId = item.getPolicyId() != null ? item.getPolicyId() : (policy != null ? policy.getPolicyId() : null);
        Integer exp = category == SimulationItemCategory.POLICY ? 100 : (category == SimulationItemCategory.EXPENSE ? 30 : 50);

        return QuestVO.builder()
            .userId(userId)
            .simulationId(simulationId)
            .simulationItemId(item.getSimulationItemId())
            .displayName(name)
            .questStatus(QuestStatus.NOT_COMPLETED)
            .questUrl(policy != null ? policy.getPolicyUrl() : null)
            .simulationItemCategory(category)
            .simulationItemExpenseCategory(item.getSimulationItemExpenseCategory())
            .simulationItemApplyAmount(item.getSimulationItemApplyAmount())
            .policyId(pId)
            .recurrenceType(recurrence)
            .policyName(policy != null ? policy.getPolicyName() : null)
            .policyUrl(policy != null ? policy.getPolicyUrl() : null)
            .policyDueDate(policy != null ? policy.getDueDate() : null)
            .expReward(exp)
            .build();
    }

    public static QuestVO of(
        QuestVO quest,
        SimulationItemVO item,
        PolicyVO policy
    ) {
        if (quest == null) {
            return item != null ? createFromSimulationItem(null, item.getSimulationId(), item, policy) : null;
        }

        if (item != null) {
            if (quest.getSimulationId() == null) quest.setSimulationId(item.getSimulationId());
            if (quest.getSimulationItemId() == null) quest.setSimulationItemId(item.getSimulationItemId());
            if (quest.getSimulationItemCategory() == null) quest.setSimulationItemCategory(item.getSimulationItemCategory());
            if (quest.getSimulationItemExpenseCategory() == null) quest.setSimulationItemExpenseCategory(item.getSimulationItemExpenseCategory());
            if (quest.getSimulationItemApplyAmount() == null) quest.setSimulationItemApplyAmount(item.getSimulationItemApplyAmount());
            if (quest.getRecurrenceType() == null) quest.setRecurrenceType(SimulationItemResponse.resolveRecurrenceType(item, policy));
            if (quest.getDisplayName() == null) quest.setDisplayName(SimulationItemResponse.resolveDisplayName(item, policy));
            if (quest.getPolicyId() == null) quest.setPolicyId(item.getPolicyId());
        }

        if (policy != null) {
            if (quest.getPolicyId() == null) quest.setPolicyId(policy.getPolicyId());
            if (quest.getPolicyName() == null) quest.setPolicyName(policy.getPolicyName());
            if (quest.getPolicyUrl() == null) quest.setPolicyUrl(policy.getPolicyUrl());
            if (quest.getQuestUrl() == null) quest.setQuestUrl(policy.getPolicyUrl());
            if (quest.getPolicyDueDate() == null) quest.setPolicyDueDate(policy.getDueDate());
        }

        if (quest.getQuestStatus() == null) {
            quest.setQuestStatus(QuestStatus.NOT_COMPLETED);
        }

        if (quest.getExpReward() == null) {
            Integer exp = quest.getSimulationItemCategory() == SimulationItemCategory.POLICY ? 100
                : (quest.getSimulationItemCategory() == SimulationItemCategory.EXPENSE ? 30 : 50);
            quest.setExpReward(exp);
        }

        return quest;
    }
}
