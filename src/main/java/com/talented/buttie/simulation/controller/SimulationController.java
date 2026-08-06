package com.talented.buttie.simulation.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.common.security.AuthenticationUser;
import com.talented.buttie.common.security.annotation.AuthUser;
import com.talented.buttie.simulation.domain.SimulationItemCategory;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.dto.request.ApplySimulationItemRequest;
import com.talented.buttie.simulation.dto.request.CreateSimulationRequest;
import com.talented.buttie.simulation.dto.request.UpdateSimulationPeriodRequest;
import com.talented.buttie.simulation.dto.response.ApplySimulationItemResponse;
import com.talented.buttie.simulation.dto.response.ConfirmedSimulationResponse;
import com.talented.buttie.simulation.dto.response.SimulationDetailResponse;
import com.talented.buttie.simulation.dto.response.SimulationItemsByCategoryResponse;
import com.talented.buttie.simulation.dto.response.SimulationItemReportResponse;
import com.talented.buttie.simulation.dto.response.SimulationResponse;
import com.talented.buttie.simulation.service.SimulationCreateService;
import com.talented.buttie.simulation.service.SimulationItemCreateService;
import com.talented.buttie.simulation.service.SimulationItemReadService;
import com.talented.buttie.simulation.service.SimulationReadService;
import com.talented.buttie.simulation.service.SimulationUpdateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Simulation")
@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
public class SimulationController {

    private final SimulationCreateService simulationCreateService;
    private final SimulationReadService simulationReadService;
    private final SimulationUpdateService simulationUpdateService;
    private final SimulationItemCreateService simulationItemCreateService;
    private final SimulationItemReadService simulationItemReadService;

    @ApiOperation("시뮬레이션 최초 생성")
    @PostMapping
    public ApplicationResponse<SimulationResponse> createSimulation(
        @AuthUser AuthenticationUser authUser,
        @Valid @RequestBody CreateSimulationRequest request
    ) {
        SimulationVO simulation = simulationCreateService.createSimulation(authUser.userId(), request);

        return ApplicationResponse.onSuccess(
            SimulationResponse.from(simulation)
        );
    }

    @ApiOperation("현재 시뮬레이션 통합 조회")
    @GetMapping
    public ApplicationResponse<SimulationDetailResponse> getSimulationTotal(
        @AuthUser AuthenticationUser authUser
    ) {
        SimulationVO simulation = simulationReadService.getCurrentSimulation(authUser.userId());

        return ApplicationResponse.onSuccess(
            SimulationDetailResponse.from(simulation)
        );
    }

    @ApiOperation("시뮬레이션 수행 기간 수정")
    @PatchMapping("/period")
    public ApplicationResponse<Void> updateSimulationPeriod(
        @AuthUser AuthenticationUser authUser,
        @Valid @RequestBody UpdateSimulationPeriodRequest request
    ) {
        simulationUpdateService.updateSimulationPeriod(authUser.userId(), request);

        return ApplicationResponse.onSuccess(null);
    }

    @ApiOperation("최근 확정 시뮬레이션 조회")
    @GetMapping("/confirmed")
    public ApplicationResponse<ConfirmedSimulationResponse> getLatestConfirmedSimulation(
        @AuthUser AuthenticationUser authUser
    ) {
        SimulationVO simulation = simulationReadService.getLatestConfirmedSimulation(authUser.userId());

        return ApplicationResponse.onSuccess(ConfirmedSimulationResponse.from(simulation));
    }

    @ApiOperation("시뮬레이션 항목 적용 확정")
    @PostMapping("/items")
    public ApplicationResponse<ApplySimulationItemResponse> applySimulationItem(
        @AuthUser AuthenticationUser authUser,
        @Valid @RequestBody ApplySimulationItemRequest request
    ) {
        ApplySimulationItemResponse response = simulationItemCreateService.applyItem(authUser.userId(), request);
        return ApplicationResponse.onSuccess(response);
    }

    @ApiOperation("시뮬레이션 카테고리별 적용된 항목 목록 조회")
    @GetMapping("/items")
    public ApplicationResponse<SimulationItemsByCategoryResponse> getAllItemsByCategory(
        @AuthUser AuthenticationUser authUser,
        @RequestParam("category") SimulationItemCategory itemCategory
    ) {
        SimulationItemsByCategoryResponse response = simulationItemReadService
            .findItemListByCategory(authUser.userId(), itemCategory);

        return ApplicationResponse.onSuccess(response);
    }

    @ApiOperation("시뮬레이션 적용 항목 결과 보고서 조회")
    @GetMapping("/items/report")
    public ApplicationResponse<SimulationItemReportResponse> getAppliedItemReport(
        @AuthUser AuthenticationUser authUser
    ) {
        SimulationItemReportResponse response = simulationItemReadService.getAppliedItemReport(authUser.userId());
        return ApplicationResponse.onSuccess(response);
    }
}
