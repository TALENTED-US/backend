package com.talented.buttie.quest.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Quest")
@RestController
@RequestMapping("/api/quest/quest")
@RequiredArgsConstructor
public class QuestController {
}
