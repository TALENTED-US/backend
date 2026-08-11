package com.talented.buttie.catalog.external.vworld;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdmCodeItem(
    @JsonProperty("admCodeNm") String admCodeNm,
    @JsonProperty("admCode") String admCode,
    @JsonProperty("lowestAdmCodeNm") String lowestAdmCodeNm
) {
    @JsonCreator
    public AdmCodeItem {
    }
}
