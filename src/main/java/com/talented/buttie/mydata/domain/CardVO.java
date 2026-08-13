package com.talented.buttie.mydata.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CardVO {

    private Long cardId;
    private Long userId;
    private Long linkedAccountId;
    private String linkedBankCode;
    private String externalCardId;
    private String cardInstitutionName;
    private String cardName;
    private CardType cardType;
    @ToString.Exclude
    private String cardNumberMasked;
    private Integer cardBalance;
    private Boolean cardIsActive;
    private LocalDateTime cardSyncedAt;
}
