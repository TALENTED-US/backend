package com.talented.buttie.catalog.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Policy")
@RestController
@RequestMapping("/api/catalog/policy")
@RequiredArgsConstructor
public class PolicyController {
}
