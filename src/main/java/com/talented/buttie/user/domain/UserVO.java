package com.talented.buttie.user.domain;

import com.talented.buttie.user.dto.request.auth.AuthSignUpRequest;
import com.talented.buttie.user.dto.response.auth.VerifiedCustomer;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    public static UserVO createWithdrawnUser(
        Long userId,
        String withdrawalIdentifier,
        String withdrawnPasswordHash
    ) {
        return UserVO.builder()
            .userId(userId)
            .userName(withdrawalIdentifier + "-" + userId)
            .userEmail("withdrawn-" + withdrawalIdentifier + "@deleted.local")
            .userPasswordHash(withdrawnPasswordHash)
            .userNickname("withdrawn-" + withdrawalIdentifier.substring(0, 20))
            .userPhoneNumber("w" + withdrawalIdentifier.substring(0, 19))
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

    public static UserVO createUser(AuthSignUpRequest authSignUpRequest, VerifiedCustomer verifiedCustomer, String hashedPassword) {
        return UserVO.builder()
            .userName(verifiedCustomer.name())
            .userEmail(authSignUpRequest.userEmail())
            .userPasswordHash(hashedPassword)
            .userNickname(authSignUpRequest.userNickname())
            .userPhoneNumber(verifiedCustomer.phoneNumber())
            .build();
    }
}
