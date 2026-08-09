package com.talented.buttie.user.dto.response.auth;

public record PortOneIdentityVerificationResponse(
    String status,
    VerifiedCustomer verifiedCustomer
) {

}
