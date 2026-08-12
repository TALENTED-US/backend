package com.talented.buttie.catalog.external.youthcenter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YouthCenterPolicyResponse(
    @JsonProperty("resultCode") int resultCode,
    @JsonProperty("resultMessage") String resultMessage,
    @JsonProperty("result") Result result
) {
    @JsonCreator
    public YouthCenterPolicyResponse {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(
        @JsonProperty("pagging") Pagging pagging,
        @JsonProperty("youthPolicyList") List<YouthCenterPolicyItem> youthPolicyList
    ) {
        @JsonCreator
        public Result {
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Pagging(
        @JsonProperty("totCount") int totCount,
        @JsonProperty("pageNum") int pageNum,
        @JsonProperty("pageSize") int pageSize
    ) {
        @JsonCreator
        public Pagging {
        }
    }
}
