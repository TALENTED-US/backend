package com.talented.buttie.user.service.auth;

import com.github.benmanes.caffeine.cache.Cache;
import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.user.dto.request.auth.AuthVerifyRequest;
import com.talented.buttie.user.dto.response.auth.AuthVerifyResponse;
import com.talented.buttie.user.dto.response.auth.PortOneIdentityVerificationResponse;
import com.talented.buttie.user.dto.response.auth.VerifiedCustomer;
import com.talented.buttie.user.exception.AuthErrorCode;
import java.util.UUID;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class PortOneService {

    @Value("${portone.api.key}")
    private String apiKey;
    private static final String PORTONE_IDENTITY_VERIFICATION_URL = "https://api.portone.io/identity-verifications";
    private final RestTemplate restTemplate;
    private final Cache<String, VerifiedCustomer> identityVerificationCache;

    public AuthVerifyResponse verifyUser(@Valid AuthVerifyRequest authVerifyRequest) {

        VerifiedCustomer verifiedCustomer = getVerification(authVerifyRequest.identityVerificationId()).verifiedCustomer();
        VerifiedCustomer formattedCustomer = VerifiedCustomer.builder()
            .name(verifiedCustomer.name())
            .birthDate(verifiedCustomer.birthDate())
            .phoneNumber(formatPhoneNumber(verifiedCustomer.phoneNumber()))
            .build();

        String verificationToken = UUID.randomUUID().toString();
        identityVerificationCache.put(verificationToken, formattedCustomer);
        return new AuthVerifyResponse(verificationToken, formattedCustomer);
    }

    private PortOneIdentityVerificationResponse getVerification(String identityId) {
        String url = PORTONE_IDENTITY_VERIFICATION_URL + "/" + identityId;

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "PortOne " + apiKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<PortOneIdentityVerificationResponse> response =
            restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                PortOneIdentityVerificationResponse.class,
                identityId
            );

        PortOneIdentityVerificationResponse body = response.getBody();

        if (body == null || !"VERIFIED".equals(body.status())) {
            throw ApplicationException.from(AuthErrorCode.IDENTITY_VERIFICATION_FAILED);
        }
        return body;
    }

    private String formatPhoneNumber(String phoneNumber) {
        String digits = phoneNumber.replaceAll("\\D", "");

        if (digits.matches("^010\\d{8}$")) {
            return digits.replaceFirst(
                "^(010)(\\d{4})(\\d{4})$",
                "$1-$2-$3"
            );
        }

        throw ApplicationException.from(AuthErrorCode.INVALID_PHONE_NUMBER);
    }

}
