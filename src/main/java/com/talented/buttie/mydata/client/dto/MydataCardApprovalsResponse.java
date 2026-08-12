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
public class MydataCardApprovalsResponse {

    @JsonProperty("rsp_code")
    private String responseCode;
    @JsonProperty("approved_cnt")
    private Integer approvedCount;
    @JsonProperty("approved_list")
    private List<MydataCardApprovalData> approvals;
}
