package com.aquaculture.controller;

import com.aquaculture.common.Result;
import com.aquaculture.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/backup")
public class BackupController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/export")
    public Result<?> export(@RequestParam("table") String table) {
        // Validate table name to prevent SQL injection
        String[] allowedTables = {
                "cage", "water_quality", "weather", "feeding", "disease",
                "staff", "alert", "alert_threshold", "trace", "feed_stock", "sys_user"
        };
        boolean valid = false;
        for (String allowed : allowedTables) {
            if (allowed.equals(table)) {
                valid = true;
                break;
            }
        }
        if (!valid) {
            return Result.fail("不支持的表名: " + table);
        }

        try {
            String sql = "SELECT * FROM " + table;
            List<Map<String, Object>> data = jdbcTemplate.queryForList(sql);
            return Result.success(data);
        } catch (Exception e) {
            return Result.fail("导出失败: " + e.getMessage());
        }
    }

    @PostMapping("/database")
    public Result<?> backupDatabase() {
        try {
            // Ensure export directory exists
            Path exportDir = Paths.get("data", "export");
            if (!Files.exists(exportDir)) {
                Files.createDirectories(exportDir);
            }

            // Source database file
            Path sourcePath = Paths.get("data", "aquaculture.db");
            if (!Files.exists(sourcePath)) {
                return Result.fail("数据库文件不存在");
            }

            // Generate backup filename with timestamp
            String timestamp = DateUtils.now().replace(" ", "_").replace(":", "-");
            String backupFileName = "aquaculture_backup_" + timestamp + ".db";
            Path targetPath = exportDir.resolve(backupFileName);

            // Copy file
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

            return Result.success("数据库备份成功", backupFileName);
        } catch (IOException e) {
            return Result.fail("数据库备份失败: " + e.getMessage());
        }
    }
}
