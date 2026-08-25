package com.talented.buttie.catalog.external.vworld;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * VWorld admCodeList/admSiList 응답 wrapper. wrapper 태그명과 item 배열명이 둘 다 admVOList.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VWorldAdmResponse(
    @JsonProperty("admVOList") Body admVOList
) {
    @JsonCreator
    public VWorldAdmResponse {
    }

    /**
     * 키 인증 실패 시 VWorld는 200 OK로 {"admVOList":{"error":"INCORRECT_KEY","message":"..."}}
     * 형태를 반환한다 (성공 응답과 같은 admVOList 키를 재사용). error/message는 그 경우에만 채워진다.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Body(
        @JsonProperty("numOfRows") int numOfRows,
        @JsonProperty("pageNo") int pageNo,
        @JsonProperty("totalCount") int totalCount,
        @JsonProperty("admVOList") List<AdmCodeItem> admVOList,
        @JsonProperty("error") String error,
        @JsonProperty("message") String message
    ) {
        @JsonCreator
        public Body {
        }
    }
}
