package com.paulo.smartpet.exception;

public class SaasAccessDeniedException extends RuntimeException {
    public SaasAccessDeniedException(String message) {
        super(message);
    }
}