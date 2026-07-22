package com.talented.buttie.notification.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Notification")
@RestController
@RequestMapping("/api/notification/notification")
@RequiredArgsConstructor
public class NotificationController {
}
