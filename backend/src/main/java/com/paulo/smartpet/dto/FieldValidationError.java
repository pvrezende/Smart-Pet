package com.paulo.smartpet.dto;

public record FieldValidationError(
        String field,
        String message
) {
}