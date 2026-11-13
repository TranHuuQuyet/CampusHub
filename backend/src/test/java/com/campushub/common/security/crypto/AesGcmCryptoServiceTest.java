package com.campushub.common.security.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AesGcmCryptoServiceTest {

    // Valid 256-bit (32 bytes) master key encoded in Base64
    private static final String VALID_BASE64_KEY = "DorOMkKIuVqD+KygE/kbnokLec6qHUXeAgvbqSRMzHg=";

    private AesGcmCryptoServiceImpl cryptoService;

    @BeforeEach
    void setUp() {
        cryptoService = new AesGcmCryptoServiceImpl(VALID_BASE64_KEY);
        cryptoService.init();
    }

    @Test
    @DisplayName("AC-1: Successfully encrypts and decrypts UTF-8 plain text using AES-256-GCM")
    void shouldEncryptAndDecryptPlainTextSuccessfully() {
        String plainText = "0912345678";

        String cipherText = cryptoService.encrypt(plainText);

        assertThat(cipherText).isNotNull();
        assertThat(cipherText).isNotEqualTo(plainText);

        String decrypted = cryptoService.decrypt(cipherText);
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    @DisplayName("AC-1: Successfully encrypts and decrypts Unicode and special characters")
    void shouldEncryptAndDecryptUnicodeAndSpecialCharacters() {
        String complexText = "Nguyễn Văn A - Thẻ SV: 2026!@#$%^&*()_+{}[]";

        String cipherText = cryptoService.encrypt(complexText);
        assertThat(cipherText).isNotNull();

        String decrypted = cryptoService.decrypt(cipherText);
        assertThat(decrypted).isEqualTo(complexText);
    }

    @Test
    @DisplayName("Null safety: Encrypting null or empty input returns null")
    void shouldReturnNullWhenEncryptingNullOrEmpty() {
        assertThat(cryptoService.encrypt(null)).isNull();
        assertThat(cryptoService.encrypt("")).isNull();
    }

    @Test
    @DisplayName("Null safety: Decrypting null or empty input returns null")
    void shouldReturnNullWhenDecryptingNullOrEmpty() {
        assertThat(cryptoService.decrypt(null)).isNull();
        assertThat(cryptoService.decrypt("")).isNull();
    }

    @Test
    @DisplayName("AC-2: Two encryptions of identical plaintext yield distinct ciphertexts (unique 12-byte IV)")
    void shouldGenerateDistinctCiphertextsForIdenticalPlaintext() {
        String plainText = "0912345678";

        String cipherText1 = cryptoService.encrypt(plainText);
        String cipherText2 = cryptoService.encrypt(plainText);

        assertThat(cipherText1).isNotNull();
        assertThat(cipherText2).isNotNull();
        assertThat(cipherText1).isNotEqualTo(cipherText2);

        // Both decrypt back to the identical plaintext
        assertThat(cryptoService.decrypt(cipherText1)).isEqualTo(plainText);
        assertThat(cryptoService.decrypt(cipherText2)).isEqualTo(plainText);
    }

    @Test
    @DisplayName("AC-3: Modifying any bit of the Base64 ciphertext triggers CryptoException")
    void shouldThrowCryptoExceptionWhenCiphertextIsTampered() {
        String plainText = "0912345678";
        String cipherText = cryptoService.encrypt(plainText);

        byte[] rawBytes = Base64.getDecoder().decode(cipherText);
        // Tamper with the last byte (part of the 128-bit authentication tag)
        rawBytes[rawBytes.length - 1] = (byte) (rawBytes[rawBytes.length - 1] ^ 0xFF);
        String tamperedCipher = Base64.getEncoder().encodeToString(rawBytes);

        assertThatThrownBy(() -> cryptoService.decrypt(tamperedCipher))
                .isInstanceOf(CryptoException.class)
                .hasMessageContaining("Decryption integrity verification failed");
    }

    @Test
    @DisplayName("Edge Case: Decrypting invalid Base64 string throws CryptoException")
    void shouldThrowCryptoExceptionForInvalidBase64() {
        assertThatThrownBy(() -> cryptoService.decrypt("!!!not-valid-base64???"))
                .isInstanceOf(CryptoException.class)
                .hasMessageContaining("input is not valid Base64");
    }

    @Test
    @DisplayName("Edge Case: Decrypting payload shorter than IV + Tag (28 bytes) throws CryptoException")
    void shouldThrowCryptoExceptionForShortPayload() {
        byte[] shortBytes = new byte[20]; // Less than 28 bytes
        String shortCipher = Base64.getEncoder().encodeToString(shortBytes);

        assertThatThrownBy(() -> cryptoService.decrypt(shortCipher))
                .isInstanceOf(CryptoException.class)
                .hasMessageContaining("shorter than minimum required");
    }

    @Test
    @DisplayName("Startup Gate: Fails fast with IllegalStateException when master key is missing or blank")
    void shouldThrowIllegalStateExceptionWhenMasterKeyIsMissing() {
        AesGcmCryptoServiceImpl serviceWithEmptyKey = new AesGcmCryptoServiceImpl("");
        assertThatThrownBy(serviceWithEmptyKey::init)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Master encryption key (app.security.encryption-key) is not configured");

        AesGcmCryptoServiceImpl serviceWithNullKey = new AesGcmCryptoServiceImpl(null);
        assertThatThrownBy(serviceWithNullKey::init)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Master encryption key (app.security.encryption-key) is not configured");
    }

    @Test
    @DisplayName("Startup Gate: Fails fast with IllegalStateException when master key is not valid Base64")
    void shouldThrowIllegalStateExceptionWhenMasterKeyIsNotBase64() {
        AesGcmCryptoServiceImpl service = new AesGcmCryptoServiceImpl("invalid!base64?key");
        assertThatThrownBy(service::init)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Master encryption key is not a valid Base64 string");
    }

    @Test
    @DisplayName("Startup Gate: Fails fast with IllegalStateException when master key length is not exactly 32 bytes")
    void shouldThrowIllegalStateExceptionWhenKeyLengthIsNot32Bytes() {
        // 16 bytes (128 bits) instead of 32 bytes
        byte[] sixteenByteKey = new byte[16];
        String base64Sixteen = Base64.getEncoder().encodeToString(sixteenByteKey);

        AesGcmCryptoServiceImpl service = new AesGcmCryptoServiceImpl(base64Sixteen);
        assertThatThrownBy(service::init)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be exactly 256 bits (32 bytes)");
    }
}
