package com.talented.buttie.mydata.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MydataCardData {

    @JsonProperty("card_id")
    private String cardId;
    @JsonProperty("card_num")
    private String cardNumberMasked;
    @JsonProperty("is_consent")
    private Boolean isConsent;
    @JsonProperty("card_name")
    private String cardName;
    @JsonProperty("card_member")
    private String cardMember;
    @JsonProperty("card_type")
    private String cardType;
    @JsonProperty("institution_name")
    private String institutionName;
}
