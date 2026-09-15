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
    void cmsMutationsDeclarePermission() throws IOException {
        String article = Files.readString(Path.of(
                "src/main/java/com/lesofn/archforge/server/admin/controller/cms/CmsArticleController.java"));
        String category = Files.readString(Path.of(
                "src/main/java/com/lesofn/archforge/server/admin/controller/cms/CmsCategoryController.java"));
        assertTrue(article.contains("cms:article:remove"));
        assertTrue(article.contains("cms:article:publish"));
        assertTrue(article.contains("cms:article:offline"));
        assertTrue(category.contains("cms:category:remove"));
    }
}
