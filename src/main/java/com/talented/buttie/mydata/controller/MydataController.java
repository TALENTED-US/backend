package com.talented.buttie.mydata.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Mydata")
@RestController
@RequestMapping("/api/mydata/mydata")
@RequiredArgsConstructor
public class MydataController {
}
