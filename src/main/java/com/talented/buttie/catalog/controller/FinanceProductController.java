package com.talented.buttie.catalog.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Finance Product")
@RestController
@RequestMapping("/api/catalog/finance-product")
@RequiredArgsConstructor
public class FinanceProductController {
}
