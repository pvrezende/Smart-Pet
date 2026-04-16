package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.BackupResponse;
import com.paulo.smartpet.exception.BusinessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Service
public class BackupService {

    private final JdbcTemplate jdbcTemplate;

    public BackupService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public BackupResponse generateBackup() {
        try {
            Path backupDir = Paths.get("backups");
            Files.createDirectories(backupDir);

            String timestamp = LocalDateTime.now()
                    .withNano(0)
                    .toString()
                    .replace(":", "-");

            String fileName = "smartpet-backup-" + timestamp + ".sql";
            Path backupFile = backupDir.resolve(fileName).toAbsolutePath();

            String normalizedPath = backupFile.toString().replace("\\", "/");
            String sql = "SCRIPT TO '" + normalizedPath + "'";

            jdbcTemplate.execute(sql);

            return new BackupResponse(
                    fileName,
                    backupFile.toString(),
                    LocalDateTime.now()
            );
        } catch (IOException ex) {
            throw new BusinessException("Não foi possível criar a pasta de backups");
        } catch (Exception ex) {
            throw new BusinessException("Não foi possível gerar o backup do banco");
        }
    }
}