package com.talented.buttie.user.domain;

import java.time.LocalDateTime;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserVO {
    private Long userId;
    private String userName;
    @ToString.Exclude
    private String userEmail;
    @ToString.Exclude
    private String userPasswordHash;
    private String userNickname;
    @ToString.Exclude
    private String userPhoneNumber;
    private Boolean userOnboardingCompleted;
    private UserStatus userStatus;
    private LocalDateTime userWithdrawnAt;
    private LocalDateTime userCreatedAt;
    private LocalDateTime userUpdatedAt;

    public static UserVO createWithdrawnUser(Long userId) {
        return UserVO.builder()
            .userId(userId)
            .userStatus(UserStatus.WITHDRAWN)
            .userWithdrawnAt(LocalDateTime.now())
            .build();
    }

    public static UserVO createModifiedUser(Long userId, String nickname) {
        return UserVO.builder()
            .userId(userId)
            .userNickname(nickname)
            .build();
    }
}
