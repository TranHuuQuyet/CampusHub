package com.campushub.common.security.crypto;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class AesGcmCryptoServiceImpl implements AesGcmCryptoService {

    private static final String ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final int MIN_ENCRYPTED_PAYLOAD_BYTES = IV_LENGTH_BYTES + (TAG_LENGTH_BITS / 8);

    private final String base64Key;
    private final SecureRandom secureRandom = new SecureRandom();
    private SecretKey secretKey;

    public AesGcmCryptoServiceImpl(@Value("${app.security.encryption-key:}") String base64Key) {
        this.base64Key = base64Key;
    }

    @PostConstruct
    public void init() {
        if (base64Key == null || base64Key.trim().isEmpty()) {
            throw new IllegalStateException("Master encryption key (app.security.encryption-key) is not configured.");
        }

        byte[] decodedKey;
        try {
            decodedKey = Base64.getDecoder().decode(base64Key.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Master encryption key is not a valid Base64 string.", e);
        }

        if (decodedKey.length != 32) {
            throw new IllegalStateException("Master encryption key must be exactly 256 bits (32 bytes). Found: " + decodedKey.length + " bytes.");
        }

        this.secretKey = new SecretKeySpec(decodedKey, ALGORITHM);
    }

    @Override
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return null;
        }

        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] cipherTextWithTag = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherTextWithTag.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherTextWithTag);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (GeneralSecurityException e) {
            throw new CryptoException("Failed to encrypt data with AES-256-GCM.", e);
        }
    }

    @Override
    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return null;
        }

        byte[] payload;
        try {
            payload = Base64.getDecoder().decode(cipherText);
        } catch (IllegalArgumentException e) {
            throw new CryptoException("Decryption failed: input is not valid Base64.", e);
        }

        if (payload.length < MIN_ENCRYPTED_PAYLOAD_BYTES) {
            throw new CryptoException("Decryption failed: payload length is shorter than minimum required [IV + Tag] length.");
        }

        try {
            ByteBuffer byteBuffer = ByteBuffer.wrap(payload);
            byte[] iv = new byte[IV_LENGTH_BYTES];
            byteBuffer.get(iv);

            byte[] cipherTextWithTag = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherTextWithTag);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] plainBytes = cipher.doFinal(cipherTextWithTag);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new CryptoException("Decryption integrity verification failed or data corrupted.", e);
        }
    }
}
