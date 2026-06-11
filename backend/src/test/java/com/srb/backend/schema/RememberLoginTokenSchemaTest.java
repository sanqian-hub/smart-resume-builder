package com.srb.backend.schema;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RememberLoginTokenSchemaTest {

    @Test
    void initSqlDefinesRememberLoginTokenTableWithCoreFieldsAndIndexes() throws IOException {
        String initSql = Files.readString(Path.of("sql", "init.sql"), StandardCharsets.UTF_8);

        assertAll(
                () -> assertTrue(initSql.contains("CREATE TABLE `remember_login_token`"), "应创建 remember_login_token 表"),
                () -> assertTrue(initSql.contains("`userId`"), "应包含 userId 字段"),
                () -> assertTrue(initSql.contains("`selector`"), "应包含 selector 字段"),
                () -> assertTrue(initSql.contains("`validatorHash`"), "应包含 validatorHash 字段"),
                () -> assertTrue(initSql.contains("`expiresAt`"), "应包含 expiresAt 字段"),
                () -> assertTrue(initSql.contains("`lastUsedAt`"), "应包含 lastUsedAt 字段"),
                () -> assertTrue(initSql.contains("`createdAt`"), "应包含 createdAt 字段"),
                () -> assertTrue(initSql.contains("`updatedAt`"), "应包含 updatedAt 字段"),
                () -> assertTrue(initSql.contains("`revoked`"), "应包含 revoked 字段"),
                () -> assertTrue(initSql.contains("UNIQUE KEY `uk_selector` (`selector`)"), "应包含 selector 唯一索引"),
                () -> assertTrue(initSql.contains("KEY `idx_userId` (`userId`)"), "应包含 userId 普通索引"),
                () -> assertTrue(initSql.contains("TRUNCATE TABLE remember_login_token;"), "初始化清表时应包含 remember_login_token")
        );
    }
}
