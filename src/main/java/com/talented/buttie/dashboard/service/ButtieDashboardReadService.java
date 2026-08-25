package com.talented.buttie.dashboard.service;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.dashboard.domain.ButtieDashboardVO;
import com.talented.buttie.dashboard.dto.ButtieDashboardResponse;
import com.talented.buttie.dashboard.exception.ButtieDashboardErrorCode;
import com.talented.buttie.dashboard.mapper.ButtieDashboardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ButtieDashboardReadService {

    private final ButtieDashboardMapper buttieDashboardMapper;

    @Transactional(readOnly = true)
    public ButtieDashboardResponse getButtieDashboard(Long userId) {
        ButtieDashboardVO buttieDashboard = buttieDashboardMapper.selectButtieDashboard(userId);

        if (buttieDashboard == null) {
            throw ApplicationException.from(ButtieDashboardErrorCode.BUTTIE_DASHBOARD_NOT_FOUND);
        }

        return ButtieDashboardResponse.from(buttieDashboard);
    }
}
