package com.talented.buttie.simulation.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.dto.request.CreateSimulationRequestDTO;
import com.talented.buttie.simulation.dto.response.SimulationResponseDTO;
import com.talented.buttie.simulation.service.SimulationCreateService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
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

    @PostMapping
    public ApplicationResponse<SimulationResponseDTO> createSimulation(
        @RequestParam Long userId,
        @RequestBody CreateSimulationRequestDTO request
    ) {
        SimulationVO simulation = simulationCreateService.createSimulation(userId, request);
        return ApplicationResponse.onSuccess(
            SimulationResponseDTO.from(simulation)
        );
    }
}
