package com.talented.buttie.common.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class PKCrypto {
    private static PKCrypto instance;

    private final String algorithm;
    private final String secret;

    private SecretKey secretKey;

    public PKCrypto(
        @Value("${crypto.algorithm}") String algorithm,
        @Value("${crypto.secret}") String secret
    ) {
        this.algorithm = algorithm;
        this.secret = secret;
    }

    @PostConstruct
    public void init() {
        this.secretKey = new SecretKeySpec(
            secret.getBytes(StandardCharsets.UTF_8),
            algorithm
        );
        instance = this;
    }

    public static PKCrypto getInstance() {
        if (instance == null) {
            throw new IllegalStateException("PKCrypto가 초기화되지 않았습니다");
        }
        return instance;
    }

    public String encryptValue(Long value) {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] valueBytes = ByteBuffer.allocate(Long.BYTES).putLong(value).array();
            byte[] encrypted = cipher.doFinal(valueBytes);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(encrypted);
        } catch (Exception e) {
            throw new IllegalStateException("암호화 실패", e);
        }
    }

    public String encryptValue(String value) {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] valueBytes = value.getBytes();
            byte[] encrypted = cipher.doFinal(valueBytes);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(encrypted);
        } catch (Exception e) {
            throw new IllegalStateException("문자열 암호화 실패", e);
        }
    }

    public Long decryptValue(String encryptedValue) {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] encryptedBytes = Base64.getUrlDecoder().decode(encryptedValue);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return ByteBuffer.wrap(decryptedBytes).getLong();
        } catch (Exception e) {
            throw new IllegalStateException("복호화 실패", e);
        }
    }

    public static String encrypt(Long value) {
        return getInstance().encryptValue(value);
    }

    public static String encrypt(String value) {
        return getInstance().encryptValue(value);
    }

    public static Long decrypt(String encryptedValue) {
        return getInstance().decryptValue(encryptedValue);
    }


}
