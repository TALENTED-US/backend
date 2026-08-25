package com.talented.buttie.catalog.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record AdminPolicyListResponse(
    List<AdminPolicyResponse> policies,
    int totalCount
) {
}
