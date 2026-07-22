package com.talented.buttie.ledger.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Custom Expense")
@RestController
@RequestMapping("/api/ledger/custom-expense")
@RequiredArgsConstructor
public class CustomExpenseController {
}
