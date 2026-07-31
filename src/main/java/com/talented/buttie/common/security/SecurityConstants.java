package com.talented.buttie.common.security;

public final class SecurityConstants {

    public static final String AUTHORIZATION_HEADER =
        "Authorization";

    public static final String BEARER_PREFIX =
        "Bearer ";

    public static final String AUTHENTICATION_USER_ATTRIBUTE =
        "authenticationUser";

    public static final String REFRESH_TOKEN_COOKIE_NAME =
        "refreshToken";

    public static final String ROLE_PREFIX =
        "ROLE_";

    public static final String ROLE_USER =
        "ROLE_USER";

    public static final String ROLE_ADMIN =
        "ROLE_ADMIN";

    private SecurityConstants() {
    }
}
