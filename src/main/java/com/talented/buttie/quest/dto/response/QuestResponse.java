package com.talented.buttie.quest.dto.response;

import com.talented.buttie.catalog.domain.PolicyVO;
import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.quest.domain.QuestStatus;
import com.talented.buttie.quest.domain.QuestVO;
import com.talented.buttie.simulation.domain.SimulationItemCategory;
import com.talented.buttie.simulation.domain.SimulationItemVO;
import com.talented.buttie.simulation.domain.SimulationRecurrenceType;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record QuestResponse(

    @ApiModelProperty(value = "암호화된 퀘스트 ID")
    String questId,

    @ApiModelProperty(value = "암호화된 시뮬레이션 ID")
    String simulationId,

    @ApiModelProperty(value = "암호화된 시뮬레이션 항목 ID")
    String simulationItemId,

    @ApiModelProperty(value = "암호화된 거래 ID")
    String transactionId,

    @ApiModelProperty(value = "항목 표시 이름", example = "청년월세 특별지원")
    String displayName,

    @ApiModelProperty(value = "시뮬레이션 항목 카테고리 (POLICY, EXPENSE, INCOME)", example = "POLICY")
    SimulationItemCategory simulationItemCategory,

    @ApiModelProperty(value = "지출 카테고리", example = "FOOD")
    ExpenseCategory expenseCategory,

    @ApiModelProperty(value = "암호화된 정책 ID")
    String policyId,

    @ApiModelProperty(value = "정부지원 정책 이름", example = "청년월세 특별지원")
    String policyName,

    @ApiModelProperty(value = "정책 신청 마감일")
    LocalDate policyDueDate,

    @ApiModelProperty(value = "반복 여부 (ONCE, MONTHLY)", example = "MONTHLY")
    SimulationRecurrenceType recurrenceType,

    @ApiModelProperty(value = "항목 적용 금액", example = "100000")
    Integer amount,

    @ApiModelProperty(value = "퀘스트 진행 상태 (NOT_COMPLETED: 진행중, COMPLETED: 완료)", example = "NOT_COMPLETED")
    QuestStatus questStatus,

    @ApiModelProperty(value = "신청 링크")
    String questUrl
) {

    public static QuestResponse from(QuestVO quest) {
        if (quest == null) return null;

        return QuestResponse.builder()
            .questId(quest.getQuestId() == null? null : PKCrypto.encrypt(quest.getQuestId()))
            .simulationId(PKCrypto.encrypt(quest.getSimulationId() == null ? null : quest.getSimulationId()))
            .simulationItemId(quest.getSimulationItemId() == null ? null : PKCrypto.encrypt(quest.getSimulationItemId()))
            .transactionId(PKCrypto.encrypt(quest.getTransactionId() == null ? null : quest.getTransactionId()))
            .displayName(quest.getDisplayName())
            .simulationItemCategory(quest.getSimulationItemCategory())
            .expenseCategory(quest.getSimulationItemExpenseCategory())
            .policyId(PKCrypto.encrypt(quest.getPolicyId() == null ? null :quest.getPolicyId()))
            .policyName(quest.getPolicyName())
            .policyDueDate(quest.getPolicyDueDate())
            .recurrenceType(quest.getRecurrenceType())
            .amount(quest.getSimulationItemApplyAmount())
            .questStatus(quest.getQuestStatus())
            .questUrl(quest.getQuestUrl())
            .build();
    }

    public static QuestResponse from(
        QuestVO quest,
        SimulationItemVO item,
        PolicyVO policy
    ) {
        return from(QuestVO.of(quest, item, policy));
    }
}
