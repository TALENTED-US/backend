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
public class MydataCardsResponse {

    @JsonProperty("rsp_code")
    private String rspCode;
    @JsonProperty("rsp_msg")
    private String rspMessage;
    @JsonProperty("card_cnt")
    private Integer cardCount;
    @JsonProperty("card_list")
    private List<MydataCardData> cardList;
}
