package com.talented.buttie.account.domain;

import java.time.LocalDateTime;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AccountVO {
    private Long accountId;
    private Long userId;
    private String externalAccountId;
    private String institutionName;
    private String accountName;
    private AccountType accountType;
    @ToString.Exclude
    private String accountNumberMasked;
    private Integer balance;
    private Boolean isActive;
    private LocalDateTime syncedAt;
}
