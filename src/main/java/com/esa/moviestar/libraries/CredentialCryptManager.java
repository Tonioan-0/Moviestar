package com.esa.moviestar.libraries;

import org.mindrot.jbcrypt.BCrypt;

//We have used the dependency BCrypt to make the password in the database not visible

public class CredentialCryptManager {

    private static final int BCRYPT_ROUNDS = 12;

    public static boolean verifyPassword(String plainTextPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            return plainTextPassword.equals(hashedPassword);
        }
    }
    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }
}