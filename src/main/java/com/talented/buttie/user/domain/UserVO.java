package com.talented.buttie.user.domain;

import java.time.LocalDateTime;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserVO {
    private Long userId;
    private String username;
    @ToString.Exclude
    private String email;
    @ToString.Exclude
    private String password;
    private String nickname;
    @ToString.Exclude
    private String phoneNumber;
    private Boolean onboardingCompleted;
    private UserStatus status;
    private Integer buttieTotalExp;
    private Integer buttieLevel;
    private LocalDateTime withdrawnAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserVO createModifiedUser(Long userId, String nickname) {
        return UserVO.builder()
            .userId(userId)
            .nickname(nickname)
            .build();
    }
}

