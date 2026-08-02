package com.talented.buttie.user.dto.response.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record TokenResponseDTO(
    String accessToken,
    @JsonIgnore String refreshToken,
    @JsonIgnore long refreshTokenExpiration
) {
}
