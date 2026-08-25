package com.talented.buttie.catalog.external.youthcenter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YouthCenterPolicyItem(
    @JsonProperty("plcyNo") String plcyNo,
    @JsonProperty("plcyNm") String plcyNm,
    @JsonProperty("lclsfNm") String lclsfNm,
    @JsonProperty("mclsfNm") String mclsfNm,
    @JsonProperty("plcySprtCn") String plcySprtCn,
    @JsonProperty("sprtTrgtMinAge") String sprtTrgtMinAge,
    @JsonProperty("sprtTrgtMaxAge") String sprtTrgtMaxAge,
    @JsonProperty("sprtTrgtAgeLmtYn") String sprtTrgtAgeLmtYn,
    @JsonProperty("zipCd") String zipCd,
    @JsonProperty("jobCd") String jobCd,
    @JsonProperty("aplyYmd") String aplyYmd,
    @JsonProperty("bizPrdBgngYmd") String bizPrdBgngYmd,
    @JsonProperty("bizPrdEndYmd") String bizPrdEndYmd,
    @JsonProperty("aplyUrlAddr") String aplyUrlAddr,
    @JsonProperty("sbmsnDcmntCn") String sbmsnDcmntCn
) {
    @JsonCreator
    public YouthCenterPolicyItem {
    }
}
