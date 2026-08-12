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
public class MydataAccountTransactionData {

    @JsonProperty("trans_dtime")
    private String transactionDateTime;
    @JsonProperty("trans_no")
    private String transactionNumber;
    @JsonProperty("trans_type")
    private String transactionType;
    @JsonProperty("trans_amt")
    private Integer transactionAmount;
    @JsonProperty("balance_amt")
    private Integer balanceAmount;
    @JsonProperty("trans_memo")
    private String transactionMemo;
}
