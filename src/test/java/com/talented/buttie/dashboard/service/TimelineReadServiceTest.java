package com.talented.buttie.dashboard.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.dashboard.domain.TimelineVO;
import com.talented.buttie.dashboard.dto.TimelineResponse;
import com.talented.buttie.dashboard.exception.TimelineErrorCode;
import com.talented.buttie.dashboard.mapper.TimelineMapper;
import com.talented.buttie.dashboard.service.TimelineReadService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TimelineReadServiceTest {

    @Mock
    private TimelineMapper timelineMapper;

    @InjectMocks
    private TimelineReadService timelineReadService;

    private Long userId;
    private Long simulationId;
    private MockedStatic<PKCrypto> pkCryptoMockedStatic;

    @BeforeEach
    void setUp() {
        userId = 1L;
        simulationId = 10L;

        pkCryptoMockedStatic = mockStatic(PKCrypto.class);
        pkCryptoMockedStatic.when(() -> PKCrypto.encrypt(simulationId)).thenReturn("encSimulation10");
        pkCryptoMockedStatic.when(() -> PKCrypto.encrypt(userId)).thenReturn("encUser1");
    }

    @AfterEach
    void tearDown() {
        pkCryptoMockedStatic.close();
    }

    @Test
    @DisplayName("?깃났: ?붾퀎 ?ъ젙 ??꾨씪???뺤긽 議고쉶")
    void getTimelineSuccess() {
        TimelineVO timelineVO = TimelineVO.builder()
            .simulationId(simulationId)
            .userId(userId)
            .currentPrepMonths(new BigDecimal("2.6"))
            .expectPrepMonths(new BigDecimal("8.2"))
            .targetEmploymentDate(LocalDate.of(2027, 1, 1))
            .livingFundThreshold(500000)
            .build();

        given(timelineMapper.findTimelineByUserId(userId)).willReturn(timelineVO);

        TimelineResponse response = timelineReadService.getTimeline(userId);

        assertNotNull(response);
        assertEquals("encSimulation10", response.simulationId());
        assertEquals("encUser1", response.userId());
        assertEquals(new BigDecimal("2.6"), response.currentPrepMonths());
        assertEquals(new BigDecimal("8.2"), response.expectPrepMonths());
        assertEquals(LocalDate.of(2027, 1, 1), response.targetEmploymentDate());
        assertEquals(500000, response.livingFundThreshold());
    }

    @Test
    @DisplayName("?ㅽ뙣: ??꾨씪???곗씠???먯껜媛 ?녿뒗 寃쎌슦 TIMELINE_NOT_FOUND ?덉쇅 諛쒖깮")
    void whenTimelineNotFound() {
        given(timelineMapper.findTimelineByUserId(userId)).willReturn(null);

        ApplicationException exception = assertThrows(ApplicationException.class,
            () -> timelineReadService.getTimeline(userId));

        assertEquals(TimelineErrorCode.TIMELINE_NOT_FOUND, exception.getCode());
    }

    @Test
    @DisplayName("?ㅽ뙣: 痍⑥뾽 以鍮??뺣낫(targetEmploymentDate ??媛 ?꾨씫??寃쎌슦 EMPLOYMENT_PREPARATION_NOT_FOUND ?덉쇅 諛쒖깮")
    void whenEmploymentPreparationNotFound() {
        TimelineVO timelineVO = TimelineVO.builder()
            .simulationId(simulationId)
            .userId(userId)
            .currentPrepMonths(new BigDecimal("2.6"))
            .expectPrepMonths(new BigDecimal("8.2"))
            .targetEmploymentDate(null)
            .livingFundThreshold(null)
            .build();

        given(timelineMapper.findTimelineByUserId(userId)).willReturn(timelineVO);

        ApplicationException exception = assertThrows(ApplicationException.class,
            () -> timelineReadService.getTimeline(userId));

        assertEquals(TimelineErrorCode.EMPLOYMENT_PREPARATION_NOT_FOUND, exception.getCode());
    }

    @Test
    @DisplayName("?ㅽ뙣: ?ㅻ깄???뺣낫(currentPrepMonths)媛 ?꾨씫??寃쎌슦 SNAPSHOT_NOT_FOUND ?덉쇅 諛쒖깮")
    void whenSnapshotNotFound() {
        TimelineVO timelineVO = TimelineVO.builder()
            .simulationId(simulationId)
            .userId(userId)
            .currentPrepMonths(null)
            .expectPrepMonths(new BigDecimal("8.2"))
            .targetEmploymentDate(LocalDate.of(2027, 1, 1))
            .livingFundThreshold(500000)
            .build();

        given(timelineMapper.findTimelineByUserId(userId)).willReturn(timelineVO);

        ApplicationException exception = assertThrows(ApplicationException.class,
            () -> timelineReadService.getTimeline(userId));

        assertEquals(TimelineErrorCode.SNAPSHOT_NOT_FOUND, exception.getCode());
    }

    @Test
    @DisplayName("?ㅽ뙣: ?쒕??덉씠???뺣낫(expectPrepMonths)媛 ?꾨씫??寃쎌슦 SIMULATION_NOT_FOUND ?덉쇅 諛쒖깮")
    void whenSimulationNotFound() {
        TimelineVO timelineVO = TimelineVO.builder()
            .simulationId(simulationId)
            .userId(userId)
            .currentPrepMonths(new BigDecimal("2.6"))
            .expectPrepMonths(null)
            .targetEmploymentDate(LocalDate.of(2027, 1, 1))
            .livingFundThreshold(500000)
            .build();

        given(timelineMapper.findTimelineByUserId(userId)).willReturn(timelineVO);

        ApplicationException exception = assertThrows(ApplicationException.class,
            () -> timelineReadService.getTimeline(userId));

        assertEquals(TimelineErrorCode.SIMULATION_NOT_FOUND, exception.getCode());
    }
}

