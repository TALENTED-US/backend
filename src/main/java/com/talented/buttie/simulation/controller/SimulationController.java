package com.talented.buttie.simulation.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.dto.request.CreateSimulationRequest;
import com.talented.buttie.simulation.service.SimulationPostService;
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

    private final SimulationPostService simulationPostService;

    @PostMapping
    public ApplicationResponse<SimulationVO> createSimulation(
        @RequestParam Long userId,
        @RequestBody CreateSimulationRequest request
    ) {
        SimulationVO simulation = simulationPostService.createSimulation(userId, request);
        return ApplicationResponse.onSuccess(simulation);
    }
}
