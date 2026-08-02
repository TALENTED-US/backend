package com.talented.buttie.simulation.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.common.security.AuthenticationUser;
import com.talented.buttie.common.security.annotation.AuthUser;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.dto.request.CreateSimulationRequestDTO;
import com.talented.buttie.simulation.dto.request.PreviewItemRequestDTO;
import com.talented.buttie.simulation.dto.request.UpdateSimulationPeriodRequestDTO;
import com.talented.buttie.simulation.dto.response.ConfirmedSimulationResponseDTO;
import com.talented.buttie.simulation.dto.response.PreviewItemResponseDTO;
import com.talented.buttie.simulation.dto.response.SimulationDetailResponseDTO;
import com.talented.buttie.simulation.dto.response.SimulationResponseDTO;
import com.talented.buttie.simulation.service.SimulationCreateService;
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
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Simulation")
@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
public class SimulationController {

    private final SimulationCreateService simulationCreateService;
    private final SimulationReadService simulationReadService;
    private final SimulationUpdateService simulationUpdateService;

    @ApiOperation("시뮬레이션 최초 생성")
    @PostMapping
    public ApplicationResponse<SimulationResponseDTO> createSimulation(
        @AuthUser AuthenticationUser authUser,
        @Valid @RequestBody CreateSimulationRequestDTO request
    ) {
        SimulationVO simulation = simulationCreateService.createSimulation(authUser.userId(), request);

        return ApplicationResponse.onSuccess(
            SimulationResponseDTO.from(simulation)
        );
    }

    @ApiOperation("현재 시뮬레이션 통합 조회")
    @GetMapping
    public ApplicationResponse<SimulationDetailResponseDTO> getSimulationTotal(
        @AuthUser AuthenticationUser authUser
    ){
        SimulationVO simulation = simulationReadService.getCurrentSimulation(authUser.userId());

        return ApplicationResponse.onSuccess(
            SimulationDetailResponseDTO.from(simulation)
        );
    }

    @ApiOperation("시뮬레이션 수행 기간 수정")
    @PatchMapping("/period")
    public ApplicationResponse<Void> updateSimulationPeriod(
        @AuthUser AuthenticationUser authUser,
        @Valid @RequestBody UpdateSimulationPeriodRequestDTO request
    ) {
        simulationUpdateService.updateSimulationPeriod(authUser.userId(), request);

        return ApplicationResponse.onSuccess(null);
    }

    @ApiOperation("최근 확정 시뮬레이션 조회")
    @GetMapping("/confirmed")
    public ApplicationResponse<ConfirmedSimulationResponseDTO> getLatestConfirmedSimulation(
        @AuthUser AuthenticationUser authUser
    ){
        SimulationVO simulation = simulationReadService.getLatestConfirmedSimulation(authUser.userId());

        return ApplicationResponse.onSuccess(ConfirmedSimulationResponseDTO.from(simulation));
    }


    @ApiOperation("시뮬레이션 항목 대입 미리보기")
    @PostMapping("/preview")
    public ApplicationResponse<PreviewItemResponseDTO> previewItemResultSimulation(
        @AuthUser AuthenticationUser authUser,
        @Valid @RequestBody PreviewItemRequestDTO request
    ){
        PreviewItemResponseDTO response = simulationCreateService.previewItemResultSimulation(authUser.userId(), request);
        return ApplicationResponse.onSuccess(response);
    }

}
