package com.talented.buttie.mydata.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MydataAuthorizationResponse {

    private String code;
    private String state;
    @JsonProperty("api_tran_id")
    private String apiTransactionId;
}
