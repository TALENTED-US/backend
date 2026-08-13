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
public class MydataCardApprovalData {

    @JsonProperty("approved_num")
    private String approvalNumber;
    @JsonProperty("approved_dtime")
    private String approvedDateTime;
    private String status;
    @JsonProperty("trans_dtime")
    private String transactionDateTime;
    @JsonProperty("merchant_name")
    private String merchantName;
    @JsonProperty("merchant_regno")
    private String merchantRegistrationNumber;
    @JsonProperty("approved_amt")
    private Integer approvedAmount;
    @JsonProperty("modified_amt")
    private Integer modifiedAmount;
    @JsonProperty("merchant_category_code")
    private String merchantCategoryCode;
    @JsonProperty("merchant_category_name")
    private String merchantCategoryName;
}
