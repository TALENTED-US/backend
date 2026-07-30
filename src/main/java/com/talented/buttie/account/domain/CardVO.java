package com.talented.buttie.account.domain;

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
public class CardVO {

    private Long cardId;
    private Long userId;
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
