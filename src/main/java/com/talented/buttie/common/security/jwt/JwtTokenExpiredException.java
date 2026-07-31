package com.talented.buttie.common.security.jwt;

public class JwtTokenExpiredException extends IllegalArgumentException {

    public JwtTokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}