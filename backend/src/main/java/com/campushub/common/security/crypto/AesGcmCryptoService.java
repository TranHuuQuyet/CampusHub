package com.campushub.common.security.crypto;

public interface AesGcmCryptoService {

    /**
     * Encrypts plaintext using AES-256-GCM with a dynamic 12-byte random IV.
     *
     * @param plainText raw sensitive string (null or blank safe)
     * @return Base64-encoded string containing [12B IV + Ciphertext + 16B Tag], or null if plainText is null
     */
    String encrypt(String plainText);

    /**
     * Decrypts Base64-encoded AES-256-GCM payload.
     *
     * @param cipherText Base64 string containing [12B IV + Ciphertext + 16B Tag]
     * @return decrypted plaintext string, or null if cipherText is null
     * @throws CryptoException if decryption fails, data is corrupted, or authentication tag verification fails
     */
    String decrypt(String cipherText);
}
