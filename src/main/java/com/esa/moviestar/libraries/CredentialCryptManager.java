package com.esa.moviestar.libraries;

import org.mindrot.jbcrypt.BCrypt;

public class CredentialCryptManager {

    private static final int BCRYPT_ROUNDS = 12;

    public static boolean verifyPassword(String plainTextPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // This happens if the stored password is not a valid BCrypt hash
            // For backward compatibility, fall back to plain text comparison
            return plainTextPassword.equals(hashedPassword);
        }
    }
    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }
}