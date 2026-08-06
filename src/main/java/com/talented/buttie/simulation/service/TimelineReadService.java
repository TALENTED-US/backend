package com.talented.buttie.simulation.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.simulation.domain.TimelineVO;
import com.talented.buttie.simulation.dto.response.TimelineResponse;
import com.talented.buttie.simulation.exception.TimelineErrorCode;
import com.talented.buttie.simulation.mapper.TimelineMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TimelineReadService {

    private final TimelineMapper timelineMapper;

    @Transactional(readOnly = true)
    public TimelineResponse getTimeline(Long userId) {
        // 1. TimelineMapper의 단일 JOIN 쿼리를 통해 3개 테이블 데이터를 한 번에 조회
        TimelineVO timelineVO = timelineMapper.findTimelineByUserId(userId);

        if (timelineVO == null) {
            throw ApplicationException.from(TimelineErrorCode.TIMELINE_NOT_FOUND);
        }

        // 2. 세부 데이터 미존재 시 해당하는 TimelineErrorCode 예외 처리
        if (timelineVO.getTargetEmploymentDate() == null || timelineVO.getLivingFundThreshold() == null) {
            throw ApplicationException.from(TimelineErrorCode.EMPLOYMENT_PREPARATION_NOT_FOUND);
        }

        if (timelineVO.getCurrentPrepMonths() == null) {
            throw ApplicationException.from(TimelineErrorCode.SNAPSHOT_NOT_FOUND);
        }

        if (timelineVO.getExpectPrepMonths() == null) {
            throw ApplicationException.from(TimelineErrorCode.SIMULATION_NOT_FOUND);
        }

        // 3. DTO로 변환하여 응답 반환
        return TimelineResponse.from(timelineVO);
    }
}
