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
}
