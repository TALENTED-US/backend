package com.talented.buttie.user.service.auth;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.user.exception.AuthErrorCode;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseCookie;

@Service
public class AuthCookieService {

    private final boolean secure;

    public AuthCookieService(@Value("${cookie.secure:true}") boolean secure) {
        this.secure = secure;
    }

    public String createCookie(
        String cookieName,
        String value,
        boolean httpOnly,
        int maxAge
    ) {
        try {
            return ResponseCookie.from(cookieName, value)
                .httpOnly(httpOnly)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofSeconds(maxAge))
                .build()
                .toString();
        } catch (IllegalArgumentException e) {
            throw ApplicationException.from(AuthErrorCode.COOKIE_CREATE_FAILED);
        }
    }
}
