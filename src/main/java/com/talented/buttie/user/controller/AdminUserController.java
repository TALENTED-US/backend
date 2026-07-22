package com.talented.buttie.user.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.talented.buttie.common.response.ApplicationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Admin User")
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    @ApiOperation("회원 정보 목록 통합 조회")
    @GetMapping
    public ApplicationResponse<Void> getUsers() {
        return null;
    }

    @ApiOperation("목업 유저 강제 추가")
    @PostMapping
    public ApplicationResponse<Void> createMockUser() {
        return null;
    }

    @ApiOperation("개별 사용자 상세 로그 조회")
    @GetMapping("/{userId}")
    public ApplicationResponse<Void> getUserDetail(@PathVariable Long userId) {
        return null;
    }

    @ApiOperation("유저 상태 제재 설정 및 권한 조정")
    @PatchMapping("/{userId}")
    public ApplicationResponse<Void> updateUserStatus(@PathVariable Long userId) {
        return null;
    }

    @ApiOperation("악성 유저 데이터 영구 삭제")
    @DeleteMapping("/{userId}")
    public ApplicationResponse<Void> deleteUser(@PathVariable Long userId) {
        return null;
    }
}
