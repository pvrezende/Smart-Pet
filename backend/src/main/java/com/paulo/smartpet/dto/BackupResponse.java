package com.paulo.smartpet.dto;

import java.time.LocalDateTime;

public record BackupResponse(
        String fileName,
        String filePath,
        LocalDateTime generatedAt
) {
}