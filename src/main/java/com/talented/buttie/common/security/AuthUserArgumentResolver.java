package com.talented.buttie.common.security;

import com.talented.buttie.common.security.annotation.AuthUser;
import javax.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class AuthUserArgumentResolver
    implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthUser.class)
            && AuthenticationUser.class.isAssignableFrom(
            parameter.getParameterType()
        );
    }

    @Override
    public Object resolveArgument(
        MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest request =
            webRequest.getNativeRequest(HttpServletRequest.class);

        if (request == null) {
            throw new IllegalStateException("HTTP 요청 정보를 찾을 수 없습니다.");
        }

        AuthenticationUser authenticationUser =
            (AuthenticationUser) request.getAttribute(
                SecurityConstants.AUTHENTICATION_USER_ATTRIBUTE
            );

        if (authenticationUser == null) {
            throw new IllegalArgumentException("인증 정보가 없습니다.");
        }

        return authenticationUser;
    }
}