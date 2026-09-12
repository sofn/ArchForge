package com.lesofn.archforge.server.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lesofn.archforge.blog.api.dao.BlogArticleRepository;
import com.lesofn.archforge.blog.api.dao.BlogCategoryRepository;
import com.lesofn.archforge.blog.api.domain.BlogArticle;
import com.lesofn.archforge.blog.api.domain.BlogCategory;
import com.lesofn.archforge.blog.testing.ArticleTestBuilder;
import com.lesofn.archforge.server.web.AbstractWebIntegrationTest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * {@link WebBlogController} 公共端点的全链路集成测试： HTTP → 拦截器白名单 → service → JPA → Flyway schema。
 *
 * @author sofn
 */
@Tag("slow")
class WebBlogApiIntegrationTest extends AbstractWebIntegrationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private BlogArticleRepository articleRepository;

    @Autowired
    private BlogCategoryRepository categoryRepository;

    @Test
    void articlesEmptyPageWhenNoData() {
        Map<String, Object> data = dataOf(get("/web/articles"));

        assertEquals(0L, numOf(data, "total"));
        assertTrue(listOf(data).isEmpty());
    }

    @Test
    void publishedArticleRoundTripsThroughPublicEndpoints() {
        BlogCategory category = categoryRepository.save(
                new BlogCategory().setName("集成测试分类").setSlug("it-category"));
        BlogArticle article = articleRepository.save(ArticleTestBuilder.anArticle()
                .withCategoryId(category.getId())
                .withSlug("it-published-article")
                .published()
                .build());

        Map<String, Object> page = dataOf(get("/web/articles"));
        List<Map<String, Object>> list = listOf(page);
        assertEquals(1L, numOf(page, "total"));
        assertEquals("it-published-article", list.get(0).get("slug"));

        Map<String, Object> detail = dataOf(get("/web/articles/it-published-article"));
        assertEquals(article.getTitle(), detail.get("title"));
        assertEquals("集成测试分类", detail.get("categoryName"));

        List<Map<String, Object>> categories = listData(get("/web/categories"));
        Map<String, Object> seeded = categories.stream()
                .filter(c -> "it-category".equals(c.get("slug")))
                .findFirst()
                .orElseThrow();
        assertEquals(1L, numOf(seeded, "articleCount"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> get(String path) {
        String response = restClient().get().uri(path).retrieve().body(String.class);
        try {
            return OBJECT_MAPPER.readValue(response, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response: " + response, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> listData(Map<String, Object> resp) {
        return (List<Map<String, Object>>) java.util.Objects.requireNonNull(resp.get("data"));
    }
}
