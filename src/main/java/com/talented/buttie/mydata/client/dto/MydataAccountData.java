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
public class MydataAccountData {

    @JsonProperty("account_num")
    private String accountNum;
    @JsonProperty("is_consent")
    private Boolean isConsent;
    @JsonProperty("prod_name")
    private String productName;
    @JsonProperty("account_type")
    private String accountType;
    @JsonProperty("account_status")
    private String accountStatus;
    @JsonProperty("institution_name")
    private String institutionName;
    @JsonProperty("account_num_masked")
    private String accountNumberMasked;
    @JsonProperty("balance_amt")
    private Integer balanceAmount;
}
