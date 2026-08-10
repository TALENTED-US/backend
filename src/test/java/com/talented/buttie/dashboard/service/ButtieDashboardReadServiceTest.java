package com.talented.buttie.dashboard.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.dashboard.domain.ButtieDashboardVO;
import com.talented.buttie.dashboard.dto.ButtieDashboardResponse;
import com.talented.buttie.dashboard.exception.ButtieDashboardErrorCode;
import com.talented.buttie.dashboard.mapper.ButtieDashboardMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ButtieDashboardReadServiceTest {

    @Mock
    private ButtieDashboardMapper buttieDashboardMapper;

    @InjectMocks
    private ButtieDashboardReadService buttieDashboardReadService;

    @Test
    @DisplayName("성공: 홈 화면 버티 대시보드 정상 조회")
    void getButtieDashboardSuccess() {
        Long userId = 1L;
        ButtieDashboardVO vo = ButtieDashboardVO.builder()
            .buttieLevel(1)
            .buttieTotalExp(19)
            .requiredExp(50)
            .buttieImageUrl("https://cdn.buttie.com/buttie/lv1_stable.png")
            .riskLevel("STABLE")
            .currentPrepMonths(new BigDecimal("1.9"))
            .expectPrepMonths(new BigDecimal("4.0"))
            .targetEmploymentDate(LocalDate.of(2027, 1, 1))
            .build();

        given(buttieDashboardMapper.selectButtieDashboard(userId)).willReturn(vo);

        ButtieDashboardResponse response = buttieDashboardReadService.getButtieDashboard(userId);

        assertNotNull(response);
        assertEquals(1, response.buttieLevel());
        assertEquals(19, response.buttieTotalExp());
        assertEquals(50, response.requiredExp());
        assertEquals("STABLE", response.riskLevel());
        assertEquals(new BigDecimal("1.9"), response.currentPrepMonths());
        assertEquals(new BigDecimal("4.0"), response.expectPrepMonths());
        assertEquals(LocalDate.of(2027, 1, 1), response.targetEmploymentDate());
    }

    @Test
    @DisplayName("실패: 대시보드 데이터가 없는 경우 BUTTIE_DASHBOARD_NOT_FOUND 예외 발생")
    void whenButtieDashboardNotFound() {
        Long userId = 999L;
        given(buttieDashboardMapper.selectButtieDashboard(userId)).willReturn(null);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> buttieDashboardReadService.getButtieDashboard(userId)
        );

        assertEquals(ButtieDashboardErrorCode.BUTTIE_DASHBOARD_NOT_FOUND, exception.getCode());
    }
}
