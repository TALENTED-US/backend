package com.talented.buttie.mydata.domain;

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
