package com.paulo.smartpet.dto;

import java.util.List;

public record ApiPageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean empty
) {
}