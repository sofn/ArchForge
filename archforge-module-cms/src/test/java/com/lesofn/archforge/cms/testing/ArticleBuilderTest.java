package com.lesofn.archforge.cms.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.lesofn.archforge.cms.api.domain.CmsArticle;
import com.lesofn.archforge.cms.api.enums.CmsArticleStatus;
import org.junit.jupiter.api.Test;

/** Verifies the default-valid contract of the cms article test data builder. */
class ArticleBuilderTest {

    @Test
    void buildsDraftArticleByDefault() {
        CmsArticle article = ArticleTestBuilder.anArticle().build();

        assertNotNull(article.getTitle());
        assertNotNull(article.getSlug());
        assertNotNull(article.getContent());
        assertEquals(CmsArticleStatus.DRAFT, article.getStatus());
        assertEquals(1L, article.getCategoryId());
    }

    @Test
    void publishedHelperSetsStatusWithoutDomainTransition() {
        CmsArticle article = ArticleTestBuilder.anArticle().published().build();

        assertEquals(CmsArticleStatus.PUBLISHED, article.getStatus());
        assertNotNull(article.getPublishTime());
    }

    @Test
    void domainPublishRejectsAlreadyPublishedArticle() {
        CmsArticle article = ArticleTestBuilder.anArticle().published().build();

        assertThrows(Exception.class, article::publish);
    }

    @Test
    void generatesUniqueSlugsAcrossBuilds() {
        CmsArticle first = ArticleTestBuilder.anArticle().build();
        CmsArticle second = ArticleTestBuilder.anArticle().build();

        assertFalse(first.getSlug().equals(second.getSlug()));
    }
}
