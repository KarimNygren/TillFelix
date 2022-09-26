package com.passwordmanager.util;

public class InvalidPasswordException extends RuntimeException implements OrderException {

    private static final long serialVersionUID = 1608362954945217854L;
    private final String reason;

    public InvalidPasswordException(String reason) {
        this.reason = reason;
    }

    @Override
    public String getReason() {
        return reason;

    }

}
