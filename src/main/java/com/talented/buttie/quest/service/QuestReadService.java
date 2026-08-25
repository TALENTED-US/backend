package com.talented.buttie.quest.service;

import com.talented.buttie.catalog.domain.PolicyVO;
import com.talented.buttie.catalog.mapper.PolicyMapper;
import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.quest.domain.QuestVO;
import com.talented.buttie.quest.dto.response.QuestResponse;
import com.talented.buttie.quest.exception.QuestErrorCode;
import com.talented.buttie.quest.mapper.QuestMapper;
import com.talented.buttie.simulation.domain.SimulationItemVO;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.mapper.SimulationItemMapper;
import com.talented.buttie.simulation.mapper.SimulationMapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuestReadService {

    private final SimulationMapper simulationMapper;
    private final SimulationItemMapper simulationItemMapper;
    private final PolicyMapper policyMapper;
    private final QuestMapper questMapper;

    @Transactional
    public List<QuestResponse> getQuests(Long userId) {
        if (userId == null) {
            throw ApplicationException.from(QuestErrorCode.INVALID_SIMULATION_ID);
        }

        // 1. 확정(적용)된 시뮬레이션 조회 및 검증
        SimulationVO simulation = simulationMapper.findLatestConfirmedByUserId(userId);

        if (simulation == null || simulation.getSimulationId() == null) {
            throw ApplicationException.from(QuestErrorCode.SIMULATION_NOT_APPLIED);
        }

        Long simulationId = simulation.getSimulationId();

        // 2. 해당 시뮬레이션에 적용된 항목 목록 조회
        List<SimulationItemVO> appliedItems = simulationItemMapper.findAllActiveBySimulationId(simulationId);
        if (appliedItems == null) {
            appliedItems = List.of();
        }

        // 3. 기존 DB QUEST 테이블에 존재하는 데이터 조회
        List<QuestVO> existingQuests = questMapper.findAllByUserId(userId);
        if ((existingQuests == null || existingQuests.isEmpty()) && simulationId != null) {
            existingQuests = questMapper.findAllBySimulationId(simulationId);
        }

        if (appliedItems.isEmpty() && (existingQuests == null || existingQuests.isEmpty())) {
            throw ApplicationException.from(QuestErrorCode.APPLIED_SIMULATION_ITEM_NOT_FOUND);
        }

        Map<Long, SimulationItemVO> itemMap = appliedItems.stream()
            .collect(Collectors.toMap(SimulationItemVO::getSimulationItemId, Function.identity(), (i1, i2) -> i1));

        Map<Long, QuestVO> questByItemMap = (existingQuests != null ? existingQuests : List.<QuestVO>of()).stream()
            .filter(q -> q != null && q.getSimulationItemId() != null)
            .collect(Collectors.toMap(QuestVO::getSimulationItemId, Function.identity(), (q1, q2) -> q1));

        List<QuestResponse> allQuestResponses = new ArrayList<>();
        Set<Long> processedQuestIds = new HashSet<>();

        // 4. 적용된 시뮬레이션 항목 처리 (QUEST 테이블에 없을 경우 동적 QuestVO 생성 및 DB 저장)
        for (SimulationItemVO item : appliedItems) {
            if (item == null || item.getSimulationItemId() == null || item.getSimulationItemCategory() == null) {
                throw ApplicationException.from(QuestErrorCode.INVALID_SIMULATION_ITEM_DATA);
            }

            PolicyVO policy = item.getPolicyId() == null
                ? null
                : policyMapper.findById(item.getPolicyId());

            QuestVO quest = questByItemMap.get(item.getSimulationItemId());

            if (quest == null) {
                quest = QuestVO.createFromSimulationItem(userId, simulationId, item, policy);
                if (quest.getDisplayName() == null || quest.getDisplayName().isBlank()) {
                    throw ApplicationException.from(QuestErrorCode.INVALID_SIMULATION_ITEM_DATA);
                }
                questMapper.save(quest);
                if (quest.getQuestId() != null) {
                    processedQuestIds.add(quest.getQuestId());
                }
            } else {
                quest = QuestVO.of(quest, item, policy);
                if (quest.getQuestId() != null) {
                    processedQuestIds.add(quest.getQuestId());
                }
            }

            QuestResponse response = QuestResponse.from(quest);
            validateQuestResponse(response);
            allQuestResponses.add(response);
        }

        // 5. QUEST 테이블 데이터 처리
        if (existingQuests != null) {
            for (QuestVO quest : existingQuests) {
                if (quest == null) continue;
                if (quest.getQuestId() != null && processedQuestIds.contains(quest.getQuestId())) {
                    continue;
                }

                SimulationItemVO item = quest.getSimulationItemId() != null
                    ? itemMap.computeIfAbsent(quest.getSimulationItemId(), simulationItemMapper::findById)
                    : null;

                PolicyVO policy = (item != null && item.getPolicyId() != null)
                    ? policyMapper.findById(item.getPolicyId())
                    : null;

                QuestResponse response = QuestResponse.from(quest, item, policy);
                validateQuestResponse(response);
                allQuestResponses.add(response);
            }
        }

        return allQuestResponses;
    }

    private void validateQuestResponse(QuestResponse response) {
        if (response == null
            || response.displayName() == null
            || response.simulationItemCategory() == null) {
            throw ApplicationException.from(QuestErrorCode.INVALID_SIMULATION_ITEM_DATA);
        }
    }
}
