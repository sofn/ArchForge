package com.lesofn.archforge.server.admin.security;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("contract")
class MutatingPermissionCoverageTest {

    @Test
    void blogMutationsDeclarePermission() throws IOException {
        String article = Files.readString(Path.of(
                "src/main/java/com/lesofn/archforge/server/admin/controller/blog/BlogArticleController.java"));
        String category = Files.readString(Path.of(
                "src/main/java/com/lesofn/archforge/server/admin/controller/blog/BlogCategoryController.java"));
        assertTrue(article.contains("blog:article:remove"));
        assertTrue(article.contains("blog:article:publish"));
        assertTrue(article.contains("blog:article:offline"));
        assertTrue(category.contains("blog:category:remove"));
    }
}
