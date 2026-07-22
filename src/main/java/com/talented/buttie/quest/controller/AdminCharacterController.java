package com.talented.buttie.quest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.talented.buttie.common.response.ApplicationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Admin Character")
@RestController
@RequestMapping("/admin/character/levels")
@RequiredArgsConstructor
public class AdminCharacterController {

    @ApiOperation("버티 레벨 진화 조건 및 프로필 이미지 조회")
    @GetMapping
    public ApplicationResponse<Void> getLevels() {
        return null;
    }

    @ApiOperation("버티 레벨 달성 조건 등록")
    @PostMapping
    public ApplicationResponse<Void> createLevel() {
        return null;
    }

    @ApiOperation("기존 레벨 진화 기준 수정")
    @PutMapping("/{levelId}")
    public ApplicationResponse<Void> updateLevel(@PathVariable Long levelId) {
        return null;
    }

    @ApiOperation("버티 레벨 삭제")
    @DeleteMapping
    public ApplicationResponse<Void> deleteLevel() {
        return null;
    }
}
