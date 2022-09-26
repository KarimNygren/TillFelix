package com.passwordmanager.util;

import com.passwordmanager.api.Password;

import java.util.UUID;

public class PasswordUtil {

    public static UUID validateUUID(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new NonExistingPasswordException("Id " + id + " is not a uuid");
        }
    }

    public static void validate(String key, String value, String date) throws InvalidPasswordException {
        if (key == null || value == null || date == null) {
            throw new InvalidPasswordException("Request is missing/has invalid params");
        }
    }

    public static void validate(Password password) {
        if (password == null
                || password.getKey() == null
                || password.getValue() == null) {
            throw new InvalidPasswordException("Password is missing/has invalid fields");
        }
    }



}
