package com.paulo.smartpet.controller;

import com.paulo.smartpet.dto.BackupResponse;
import com.paulo.smartpet.service.BackupService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/backups")
public class BackupController {

    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BackupResponse generateBackup() {
        return backupService.generateBackup();
    }
}