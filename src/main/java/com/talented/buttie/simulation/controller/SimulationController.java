package com.talented.buttie.simulation.controller;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.common.security.AuthenticationUser;
import com.talented.buttie.common.security.annotation.AuthUser;
import com.talented.buttie.common.util.PKCrypto;
import com.talented.buttie.simulation.domain.SimulationItemCategory;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.dto.request.ApplySimulationItemRequest;
import com.talented.buttie.simulation.dto.request.CreateSimulationRequest;
import com.talented.buttie.simulation.dto.request.UpdateSimulationItemRequest;
import com.talented.buttie.simulation.dto.request.UpdateSimulationPeriodRequest;
import com.talented.buttie.simulation.dto.response.ApplySimulationItemResponse;
import com.talented.buttie.simulation.dto.response.ConfirmedSimulationResponse;
import com.talented.buttie.simulation.dto.response.SimulationDetailResponse;
import com.talented.buttie.simulation.dto.response.SimulationItemResponse;
import com.talented.buttie.simulation.dto.response.SimulationItemsByCategoryResponse;
import com.talented.buttie.simulation.dto.response.SimulationItemReportResponse;
import com.talented.buttie.simulation.dto.response.SimulationResponse;
import com.talented.buttie.simulation.exception.SimulationErrorCode;
import com.talented.buttie.simulation.service.SimulationCreateService;
import com.talented.buttie.simulation.service.SimulationItemCreateService;
import com.talented.buttie.simulation.service.SimulationItemReadService;
import com.talented.buttie.simulation.service.SimulationItemUpdateService;
import com.talented.buttie.simulation.service.SimulationReadService;
import com.talented.buttie.simulation.service.SimulationUpdateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.math.BigDecimal;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    private final SimulationItemUpdateService simulationItemUpdateService;

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

    @ApiOperation("현재 시뮬레이션 조회")
    @GetMapping
    public ApplicationResponse<SimulationDetailResponse> getSimulationTotal(
        @AuthUser AuthenticationUser authUser
    ) {
        SimulationVO simulation = simulationReadService.getCurrentSimulation(authUser.userId());

        BigDecimal currentPrepMonths = simulationItemReadService.getCurrentPrepMonths(authUser.userId());
        List<SimulationItemResponse> appliedItems = simulationItemReadService.findAllAppliedItems(simulation);

        return ApplicationResponse.onSuccess(
            SimulationDetailResponse.from(simulation, currentPrepMonths, appliedItems)
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

        BigDecimal currentPrepMonths = simulationItemReadService.getCurrentPrepMonths(authUser.userId());
        List<SimulationItemResponse> appliedItems = simulationItemReadService.findAllAppliedItems(simulation);

        return ApplicationResponse.onSuccess(
            ConfirmedSimulationResponse.from(simulation, currentPrepMonths, appliedItems)
        );
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

    @ApiOperation("항목 조건 수정")
    @PutMapping("/items/{encryptedItemId}")
    public ApplicationResponse<SimulationItemResponse> updateItemCondition(
        @AuthUser AuthenticationUser authUser,
        @PathVariable String encryptedItemId,
        @Valid @RequestBody UpdateSimulationItemRequest request
    ) {
        Long itemId = decryptItemId(encryptedItemId);

        SimulationItemResponse response =
            simulationItemUpdateService.updateItem(authUser.userId(), itemId, request);

        return ApplicationResponse.onSuccess(response);
    }

    private Long decryptItemId(String encryptedItemId) {
        try {
            return PKCrypto.decrypt(encryptedItemId);
        } catch (IllegalStateException e) {
            throw ApplicationException.from(SimulationErrorCode.INVALID_SIMULATION_ITEM);
        }
    }
}
