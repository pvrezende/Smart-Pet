package com.paulo.smartpet.exception;

public class BillingAccessDeniedException extends RuntimeException {
    public BillingAccessDeniedException(String message) {
        super(message);
    }
}