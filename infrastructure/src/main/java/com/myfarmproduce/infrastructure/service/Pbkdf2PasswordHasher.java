package com.myfarmproduce.infrastructure.service;

import com.myfarmproduce.application.service.PasswordHasher;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/** PBKDF2 (SHA-256) password hasher - dependency-free, no full Spring Security UserDetailsService hashing. */
@Component
public class Pbkdf2PasswordHasher implements PasswordHasher {

    private static final int SALT_SIZE = 16;
    private static final int KEY_SIZE_BITS = 256;
    private static final int ITERATIONS = 100_000;

    @Override
    public String hash(String password) {
        byte[] salt = new byte[SALT_SIZE];
        new SecureRandom().nextBytes(salt);
        byte[] key = pbkdf2(password, salt, ITERATIONS, KEY_SIZE_BITS);
        return ITERATIONS + "." + Base64.getEncoder().encodeToString(salt) + "." + Base64.getEncoder().encodeToString(key);
    }

    @Override
    public boolean verify(String hash, String password) {
        String[] parts = hash.split("\\.", 3);
        if (parts.length != 3) return false;

        int iterations;
        try {
            iterations = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            return false;
        }

        byte[] salt = Base64.getDecoder().decode(parts[1]);
        byte[] expected = Base64.getDecoder().decode(parts[2]);
        byte[] actual = pbkdf2(password, salt, iterations, expected.length * 8);
        return MessageDigest.isEqual(actual, expected);
    }

    private static byte[] pbkdf2(String password, byte[] salt, int iterations, int keyLengthBits) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLengthBits);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }
    }
}
