package com.talented.buttie.account.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Account")
@RestController
@RequestMapping("/api/account/account")
@RequiredArgsConstructor
public class AccountController {
}
