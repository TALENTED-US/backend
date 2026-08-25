package com.talented.buttie.common.security;

import lombok.Builder;

@Builder
public record AuthenticationUser(
    Long accountId,
    AccountType accountType,
    AdminRole adminRole
) {

    public static AuthenticationUser user(Long userId){
        return AuthenticationUser.builder()
            .accountId(userId)
            .accountType(AccountType.USER)
            .adminRole(null)
            .build();
    }

    public static AuthenticationUser admin(Long adminId, AdminRole adminRole){
        return AuthenticationUser.builder()
            .accountId(adminId)
            .accountType(AccountType.ADMIN)
            .adminRole(adminRole)
            .build();
    }

    public boolean isUser() {
        return accountType == AccountType.USER;
    }

    public boolean isAdmin() {
        return accountType == AccountType.ADMIN;
    }

    public Long userId(){
        if(!isUser()) throw new IllegalStateException("일반 사용자 계정이 아닙니다.");
        return accountId;
    }

    public Long adminId(){
        if(!isAdmin()) throw new IllegalStateException("관리자 계정이 아닙니다.");
        return accountId;
    }
}
