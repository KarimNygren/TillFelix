package com.passwordmanager.util;

public class NonExistingPasswordException extends RuntimeException implements OrderException {

    private static final long serialVersionUID = 4206494954916219654L;

    private final String reason;

    public NonExistingPasswordException(String reason) {
        super();
        this.reason = reason;
    }

    @Override
    public String getReason() {
        return reason;
    }
}
