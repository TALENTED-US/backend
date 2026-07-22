package com.talented.buttie.simulation.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Simulation")
@RestController
@RequestMapping("/api/simulation/simulation")
@RequiredArgsConstructor
public class SimulationController {
}
