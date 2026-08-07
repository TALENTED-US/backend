package com.talented.buttie.user.service.auth;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.user.exception.AuthErrorCode;
import javax.servlet.http.Cookie;
import org.springframework.stereotype.Service;

@Service
public class AuthCookieService {

    public Cookie createCookie(
        String cookieName,
        String value,
        boolean httpOnly,
        int maxAge
    ) {
        try {
            Cookie cookie = new Cookie(cookieName, value);
            cookie.setHttpOnly(httpOnly);
            cookie.setPath("/");
            cookie.setMaxAge(maxAge);
            return cookie;
        } catch (IllegalArgumentException e) {
            throw ApplicationException.from(AuthErrorCode.COOKIE_CREATE_FAILED);
        }
    }
}
