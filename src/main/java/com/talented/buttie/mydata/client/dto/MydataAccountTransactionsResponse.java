package com.talented.buttie.mydata.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MydataAccountTransactionsResponse {

    @JsonProperty("rsp_code")
    private String responseCode;
    @JsonProperty("trans_cnt")
    private Integer transactionCount;
    @JsonProperty("trans_list")
    private List<MydataAccountTransactionData> transactions;
}
