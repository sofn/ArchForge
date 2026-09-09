package com.lesofn.archforge.server.admin.security;

import static org.junit.jupiter.api.Assertions.assertTrue;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("contract")
class AdminPermissionCoverageTest {

    @Test
    void adminControllersDeclareLoginOrRoleGuard() throws IOException {
        Path controllerDir = Path.of("src/main/java/com/lesofn/archforge/server/admin/controller");
        try (Stream<Path> files = Files.walk(controllerDir)) {
            files.filter(path -> {
                java.nio.file.Path name1 = path.getFileName();
                return name1 != null && name1.toString().endsWith("Controller.java");
            }).forEach(path -> {
                try {
                    String source = Files.readString(path);
                    java.nio.file.Path name2 = path.getFileName();
                    if (name2 != null && name2.toString().equals("LoginController.java")) {
                        return;
                    }
                    assertTrue(
                            source.contains(SaCheckLogin.class.getSimpleName()) || source.contains(SaCheckRole.class
                                    .getSimpleName()),
                            () -> "Missing login/role annotation: " + path);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            });
        }
    }
}
