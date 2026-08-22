package com.lesofn.archforge.blog.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.lesofn.archforge.blog.api.domain.BlogArticle;
import com.lesofn.archforge.blog.api.enums.BlogArticleStatus;
import org.junit.jupiter.api.Test;

/** Verifies the default-valid contract of the blog article test data builder. */
class ArticleBuilderTest {

    @Test
    void buildsDraftArticleByDefault() {
        BlogArticle article = ArticleTestBuilder.anArticle().build();

        assertNotNull(article.getTitle());
        assertNotNull(article.getSlug());
        assertNotNull(article.getContent());
        assertEquals(BlogArticleStatus.DRAFT, article.getStatus());
        assertEquals(1L, article.getCategoryId());
    }

    @Test
    void publishedHelperSetsStatusWithoutDomainTransition() {
        BlogArticle article = ArticleTestBuilder.anArticle().published().build();

        assertEquals(BlogArticleStatus.PUBLISHED, article.getStatus());
        assertNotNull(article.getPublishTime());
    }

    @Test
    void domainPublishRejectsAlreadyPublishedArticle() {
        BlogArticle article = ArticleTestBuilder.anArticle().published().build();

        assertThrows(Exception.class, article::publish);
    }

    @Test
    void generatesUniqueSlugsAcrossBuilds() {
        BlogArticle first = ArticleTestBuilder.anArticle().build();
        BlogArticle second = ArticleTestBuilder.anArticle().build();

        assertFalse(first.getSlug().equals(second.getSlug()));
    }
}
