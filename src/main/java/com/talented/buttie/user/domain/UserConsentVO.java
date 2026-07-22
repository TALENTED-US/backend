package com.talented.buttie.user.domain;

import java.time.LocalDateTime;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserConsentVO {
    private Long userId;
    private LocalDateTime termsAgreedAt;
    private LocalDateTime privacyAgreedAt;
    private LocalDateTime financialInfoAgreedAt;
}
