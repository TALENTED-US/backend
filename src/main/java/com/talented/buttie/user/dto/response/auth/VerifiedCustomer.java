package com.talented.buttie.user.dto.response.auth;

import lombok.Builder;

@Builder
public record VerifiedCustomer(
    String name,
    String birthDate,
    String phoneNumber
) {

}